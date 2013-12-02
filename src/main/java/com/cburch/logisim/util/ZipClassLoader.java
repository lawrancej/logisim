/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipClassLoader extends ClassLoader {
    // This code was posted on a forum by "leukbr" on March 30, 2001.
    // http://forums.sun.com/thread.jspa?threadID=360060&forumID=31
    // I've modified it substantially to include a thread that keeps the file
    // open for OPEN_TIME milliseconds so time isn't wasted continually
    // opening and closing the file.
    private static final int OPEN_TIME = 5000;
    private static final int DEBUG = 0;
        // 0 = no debug messages
        // 1 = open/close ZIP file only
        // 2 = also each resource request
        // 3 = all messages while retrieving resource

    private static final int REQUEST_FIND = 0;
    private static final int REQUEST_LOAD = 1;

    private static class Request {
        int action;
        String resource;
        boolean responseSent;
        Object response;

        Request(int action, String resource) {
            this.action = action;
            this.resource = resource;
            this.responseSent = false;
        }

        @Override
        public String toString() {
            String act = action == REQUEST_LOAD ? "load"
                    : action == REQUEST_FIND ? "find" : "act" + action;
            return act + ":" + resource;
        }

        void setResponse(Object value) {
            synchronized(this) {
                response = value;
                responseSent = true;
                notifyAll();
            }
        }

        void ensureDone() {
            boolean aborted = false;
            synchronized(this) {
                if (!responseSent) {
                    aborted = true;
                    responseSent = true;
                    response = null;
                    notifyAll();
                }
            }
            if (aborted && DEBUG >= 1) {
                //OK
                System.err.println("request not handled successfully");
            }
        }

        Object getResponse() {
            synchronized(this) {
                while (!responseSent) {
                    try { this.wait(1000); } catch (InterruptedException e) { }
                }
                return response;
            }
        }
    }

    private class WorkThread extends Thread {
        private LinkedList<Request> requests = new LinkedList<Request>();
        private ZipFile zipFile = null;

        @Override
        public void run() {
            try {
                while (true) {
                    Request request = waitForNextRequest();
                    if (request == null) return;

                    //OK
                    if (DEBUG >= 2) System.err.println("processing " + request);
                    try {
                        switch (request.action) {
                        case REQUEST_LOAD: performLoad(request); break;
                        case REQUEST_FIND: performFind(request); break;
                        }
                    } finally {
                        request.ensureDone();
                    }
                    //OK
                    if (DEBUG >= 2) System.err.println("processed: " + request.getResponse());
                }
            } catch (Exception t) {
                //OK
                if (DEBUG >= 3) { System.err.print("uncaught: "); t.printStackTrace(); }
            } finally {
                if (zipFile != null) {
                    try {
                        zipFile.close();
                        zipFile = null;
                        //OK
                        if (DEBUG >= 1) System.err.println("  ZIP closed");
                    } catch (IOException e) {
                        //OK
                        if (DEBUG >= 1) System.err.println("Error closing ZIP file");
                    }
                }
            }
        }

        private Request waitForNextRequest() {
            synchronized(bgLock) {
                long start = System.currentTimeMillis();
                while (requests.isEmpty()) {
                    long elapse = System.currentTimeMillis() - start;
                    if (elapse >= OPEN_TIME) {
                        bgThread = null;
                        return null;
                    }
                    try {
                        bgLock.wait(OPEN_TIME);
                    } catch (InterruptedException e) { }
                }
                return requests.removeFirst();
            }
        }

        private void performFind(Request req) {
            ensureZipOpen();
            Object ret = null;
            try {
                if (zipFile != null) {
                    //OK
                    if (DEBUG >= 3) System.err.println("  retrieve ZIP entry");
                    String res = req.resource;
                    ZipEntry zipEntry = zipFile.getEntry(res);
                    if (zipEntry != null) {
                        String url = "jar:" + zipPath.toURI() + "!/" + res;
                        ret = new URL(url);
                        //OK
                        if (DEBUG >= 3) System.err.println("  found: " + url);
                    }
                }
            } catch (Exception ex) {
                //OK
                if (DEBUG >= 3) System.err.println("  error retrieving data");
                ex.printStackTrace();
            }
            req.setResponse(ret);
        }

        private void performLoad(Request req) {
            BufferedInputStream bis = null;
            ensureZipOpen();
            Object ret = null;
            try {
                if (zipFile != null) {
                    //OK
                    if (DEBUG >= 3) System.err.println("  retrieve ZIP entry");
                    ZipEntry zipEntry = zipFile.getEntry(req.resource);
                    if (zipEntry != null) {
                        //OK
                        if (DEBUG >= 3) System.err.println("  load file");
                        byte[] result = new byte[(int) zipEntry.getSize()];
                        bis = new BufferedInputStream(zipFile.getInputStream(zipEntry));
                        try {
                            bis.read(result, 0, result.length);
                            ret = result;
                        } catch (IOException e) {
                            //OK
                            if (DEBUG >= 3) System.err.println("  error loading file");
                        }
                    }
                }
            } catch (Exception ex) {
                //OK
                if (DEBUG >= 3) System.err.println("  error retrieving data");
                ex.printStackTrace();
            } finally {
                if (bis!=null) {
                    try {
                        //OK
                        if (DEBUG >= 3) System.err.println("  close file");
                        bis.close();
                    } catch (IOException ioex) {
                        //OK
                        if (DEBUG >= 3) System.err.println("  error closing data");
                    }
                }
            }
            req.setResponse(ret);
        }

        private void ensureZipOpen() {
            if (zipFile == null) {
                try {
                    //OK
                    if (DEBUG >= 3) System.err.println("  open ZIP file");
                    zipFile = new ZipFile(zipPath);
                    //OK
                    if (DEBUG >= 1) System.err.println("  ZIP opened");
                } catch (IOException e) {
                    //OK
                    if (DEBUG >= 1) System.err.println("  error opening ZIP file");
                }
            }
        }
    }

    private File zipPath;
    private HashMap<String,Object> classes = new HashMap<String,Object>();
    private Object bgLock = new Object();
    private WorkThread bgThread = null;

    public ZipClassLoader(String zipFileName) {
        this(new File(zipFileName));
    }

    public ZipClassLoader(File zipFile) {
        zipPath = zipFile;
    }

    @Override
    public URL findResource(String resourceName) {
        //OK
        if (DEBUG >= 3) System.err.println("findResource " + resourceName);
        Object ret = request(REQUEST_FIND, resourceName);
        if (ret instanceof URL) {
            return (URL) ret;
        } else {
            return super.findResource(resourceName);
        }
    }

    @Override
    public Class<?> findClass(String className) throws ClassNotFoundException {
        boolean found = false;
        Object result = null;

        // check whether we have loaded this class before
        synchronized(classes) {
            found = classes.containsKey(className);
            if (found) result = classes.get(className);
        }

        // try loading it from the ZIP file if we haven't
        if (!found) {
            String resourceName = className.replace('.', '/') + ".class";
            result = request(REQUEST_LOAD, resourceName);

            if (result instanceof byte[]) {
                //OK
                if (DEBUG >= 3) System.err.println("  define class");
                byte[] data = (byte[]) result;
                result = defineClass(className, data, 0, data.length);
                if (result != null) {
                    //OK
                    if (DEBUG >= 3) System.err.println("  class defined");
                } else {
                    //OK
                    if (DEBUG >= 3) System.err.println("  format error");
                    result = new ClassFormatError(className);
                }
            }

            synchronized(classes) { classes.put(className, result); }
        }

        if (result instanceof Class) {
            return (Class<?>) result;
        } else if (result instanceof ClassNotFoundException) {
            throw (ClassNotFoundException) result;
        } else if (result instanceof Error) {
            throw (Error) result;
        } else {
            return super.findClass(className);
        }
    }

    private Object request(int action, String resourceName) {
        Request request;
        synchronized(bgLock) {
            // start the thread if it isn't working
            if (bgThread == null) {
                bgThread = new WorkThread();
                bgThread.start();
            }
            request = new Request(action, resourceName);
            bgThread.requests.addLast(request);
            bgLock.notifyAll();
        }
        return request.getResponse();
    }
}
