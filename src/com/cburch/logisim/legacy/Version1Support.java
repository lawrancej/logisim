/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.legacy;

// missing:
//   using unsupported RAM component

import java.util.StringTokenizer;
import java.awt.Font;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import com.cburch.logisim.util.FontUtil;

public class Version1Support {
    private static class AttributeHandler {
        void handleKey(String key, GroupedReader in,
                PrintWriter out) throws IOException {
            in.startGroup();
            while (!in.atGroupEnd()) in.readLine();
            in.endGroup();
            throw new IOException("unrecognized key `" + key + "'");
        }

        boolean handleLabelKey(String key, GroupedReader in,
                PrintWriter out) throws IOException {
            if (key.equals("text")) {
                String value = in.readGroup();
                addAttribute(out, "text", value);
                return true;
            } else if (key.equals("font")) {
                String desc = in.readGroup();
                StringTokenizer toks = new StringTokenizer(desc);
                if (!toks.hasMoreTokens()) throw new IOException("font bad [0]");
                String name = toks.nextToken();
                if (!toks.hasMoreTokens()) throw new IOException("font bad [1]");
                int size = Integer.parseInt(toks.nextToken());
                if (!toks.hasMoreTokens()) throw new IOException("font bad [2]");
                String style_str = toks.nextToken();
                if (toks.hasMoreTokens()) throw new IOException("font bad [3]");
                int styleBits = 0;
                for (int i = 0; i < style_str.length(); i++) {
                    switch (style_str.charAt(i)) {
                    case 'b': styleBits |= Font.BOLD; break;
                    case 'i': styleBits |= Font.ITALIC; break;
                    case '-': break;
                    }
                }
                String style = FontUtil.toStyleStandardString(styleBits);
                addAttribute(out, "font", name + " " + style + " " + size);
                return true;
            } else {
                return false;
            }
        }
    }

    private static class TextHandler extends AttributeHandler {
        @Override
        void handleKey(String key, GroupedReader in,
                PrintWriter out) throws java.io.IOException {
            if (key.equals("halign")) {
                addAttribute(out, "halign", in.readGroup());
            } else if (key.equals("valign")) {
                String value = in.readGroup();
                if (value.equals("baseline")) value = "base";
                addAttribute(out, "valign", value);
            } else if (!handleLabelKey(key, in, out)) {
                super.handleKey(key, in, out);
            }
        }
    }

    private static class LedHandler extends AttributeHandler {
        @Override
        void handleKey(String key, GroupedReader in,
                PrintWriter out) throws java.io.IOException {
            if (key.equals("labelpos")) {
                addAttribute(out, "labelloc", in.readGroup());
            } else if (!handleLabelKey(key, in, out)) {
                super.handleKey(key, in, out);
            }
        }
    }

    private static class SwitchHandler extends AttributeHandler {
        @Override
        void handleKey(String key, GroupedReader in,
                PrintWriter out) throws java.io.IOException {
            if (key.equals("labelpos")) {
                addAttribute(out, "labelloc", in.readGroup());
            } else if (!handleLabelKey(key, in, out)) {
                super.handleKey(key, in, out);
            }
        }
    }

    private static class ConstantHandler extends AttributeHandler {
        @Override
        void handleKey(String key, GroupedReader in,
                PrintWriter out) throws IOException {
            if (key.equals("value")) {
                String val = in.readGroup();
                if (val.equals("false")) {
                    addAttribute(out, "value", "0x0");
                } else if (val.equals("true")) {
                    addAttribute(out, "value", "0x1");
                }
            } else {
                super.handleKey(key, in, out);
            }
        }
    }

    private static class ClockHandler extends AttributeHandler {
        @Override
        void handleKey(String key, GroupedReader in,
                PrintWriter out) throws IOException {
            if (key.equals("freq")) {
                in.readGroup(); // this was never anything other than 1 - ignore it
            } else {
                super.handleKey(key, in, out);
            }
        }
    }

    private Reader inFile;
    private Writer outFile;
    private boolean usesMemory = false;
    private boolean usesLegacy = false;
    private boolean usesSubcircuits = false;
    private boolean usesRam = false;

    private Version1Support(Reader inFile, Writer outFile) {
        this.inFile = inFile;
        this.outFile = outFile;
    }

    private void translate() throws IOException {
        String[] headerLines = {
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
            "<project version=\"1.0\">",
            " <lib name=\"0\" desc=\"#Base\" />",
            " <lib name=\"1\" desc=\"#Gates\" />",
            " <mappings>",
            "  <tool lib=\"0\" name=\"Menu Tool\" map=\"Button2\" />",
            "  <tool lib=\"0\" name=\"Menu Tool\" map=\"Button3\" />",
            "  <tool lib=\"0\" name=\"Menu Tool\" map=\"Ctrl Button1\" />",
            " </mappings>",
            "",
            " <toolbar>",
            "  <tool lib=\"0\" name=\"Poke Tool\" />",
            "  <tool lib=\"0\" name=\"Select Tool\" />",
            "  <tool lib=\"0\" name=\"Wiring Tool\" />",
            "  <tool lib=\"0\" name=\"Text Tool\">",
            "    <a name=\"font\" val=\"SansSerif plain 12\" />",
            "  </tool>",
            "  <sep />",
            "  <tool lib=\"0\" name=\"Pin\">",
            "   <a name=\"tristate\" val=\"false\" />",
            "  </tool>",
            "  <tool lib=\"0\" name=\"Pin\">",
            "   <a name=\"facing\" val=\"west\" />",
            "   <a name=\"labelloc\" val=\"east\" />",
            "   <a name=\"output\" val=\"true\" />",
            "  </tool>",
            "  <tool lib=\"1\" name=\"NOT Gate\" />",
            "  <tool lib=\"1\" name=\"AND Gate\" />",
            "  <tool lib=\"1\" name=\"OR Gate\" />",
            " </toolbar>",
        };
        LineNumberReader inLine = new LineNumberReader(inFile);
        PrintWriter out = new PrintWriter(outFile);
        try {
            String header = inLine.readLine();
            if (header == null) {
                throw new IOException("empty file");
            } else if (header.equals("Logisim v1.0")) {
                for (int i = 0; i < headerLines.length; i++) {
                    out.println(headerLines[i]);
                }
                GroupedReader in = new GroupedReader(inLine);
                while (!in.atFileEnd()) {
                    String key = in.readLine().trim();
                    if (key != null && key.length() > 0) {
                        handleProjectKey(key, in, out);
                    }
                    if (in.atGroupStart()) {
                        in.startGroup();
                        while (!in.atGroupEnd()) in.readLine();
                        in.endGroup();
                    }
                }
                if (usesLegacy) {
                    out.println(" <lib name=\"2\" desc=\"#Legacy\" />");
                }
                if (usesMemory) {
                    out.println(" <lib name=\"3\" desc=\"#Memory\" />");
                }
                if (usesSubcircuits) {
                    out.println(" <message value=\""
                            + Strings.get("version1SubcircuitMessage")
                            + "\" />");
                }
                if (usesRam) {
                    out.println(" <message value=\""
                            + Strings.get("version1RamMessage")
                            + "\" />");
                }
                out.println("</project>");
            } else {
                throw new IOException("Unsupported version");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("line " + inLine.getLineNumber()
                + ": " + e.getMessage());
        }
    }

    private void handleProjectKey(String key, GroupedReader in,
            PrintWriter out) throws IOException {
        if (key.equals("subcircuit")) {
            in.startGroup();
            handleCircuitData(in, out);
            in.endGroup();
        } else if (key.equals("main")) {
            String mainName = in.readGroup();
            out.println(" <main name=\"" + mainName + "\" />");
        } else {
            in.startGroup();
            while (!in.atGroupEnd()) in.readLine();
            in.endGroup();
            throw new IOException("unrecognized key " + key);
        }
    }

    private void handleCircuitData(GroupedReader in,
            PrintWriter out) throws IOException {
        String name = "main";
        StringWriter contentsSource = new StringWriter();
        PrintWriter contents = new PrintWriter(contentsSource);
        while (!in.atGroupEnd()) {
            String key = in.readLine().trim();
            if (key != null && key.length() > 0) {
                if (key.equals("name")) {
                    name = in.readGroup();
                } else {
                    handleComponent(key, in, contents);
                }
            }
            if (in.atGroupStart()) {
                in.startGroup();
                while (!in.atGroupEnd()) in.readLine();
                in.endGroup();
            }
        }
        out.println(" <circuit name=\"" + name + "\">");
        out.print(contentsSource);
        out.println(" </circuit>");
    }

    private void handleComponent(String key, GroupedReader in,
            PrintWriter out) throws IOException {
        if (key.startsWith("logisim.")) {
            key = key.substring("logisim.".length());
        }

        String libName = null;
        String compName = null;
        StringWriter contentsSource = new StringWriter();
        PrintWriter contents = new PrintWriter(contentsSource);
        AttributeHandler handler = new AttributeHandler();

        if (key.equals("WireTree")) {
            handleWireTree(in, out);
            return;
        } else if (key.equals("Subcircuit")) {
            usesSubcircuits = true;
            handleSubcircuit(in, out);
            return;
        } else if (key.equals("CirComponentAndSmall")) {
            libName = "1";
            compName = "AND Gate";
            addAttribute(contents, "size", "30");
            addAttribute(contents, "inputs", "3");
        } else if (key.equals("CirComponentOrSmall")) {
            libName = "1";
            compName = "OR Gate";
            addAttribute(contents, "size", "30");
            addAttribute(contents, "inputs", "3");
        } else if (key.equals("CirComponentNotSmall")) {
            libName = "1";
            compName = "NOT Gate";
            addAttribute(contents, "size", "20");
            addAttribute(contents, "inputs", "3");
        } else if (key.equals("CirComponentAnd")) {
            libName = "1";
            compName = "AND Gate";
        } else if (key.equals("CirComponentOr")) {
            libName = "1";
            compName = "OR Gate";
        } else if (key.equals("CirComponentNot")) {
            libName = "1";
            compName = "NOT Gate";
        } else if (key.equals("CirComponentText")) {
            libName = "0";
            compName = "Text";
            handler = new TextHandler();
        } else if (key.equals("CirComponentLED")) {
            libName = "0";
            compName = "Pin";
            addAttribute(contents, "facing", "west");
            addAttribute(contents, "output", "true");
            handler = new LedHandler();
        } else if (key.equals("CirComponentSwitch")) {
            libName = "0";
            compName = "Pin";
            addAttribute(contents, "facing", "east");
            addAttribute(contents, "output", "false");
            addAttribute(contents, "tristate", "false");
            handler = new SwitchHandler();
        } else if (key.equals("CirComponentDFlipFlop")) {
            usesLegacy = true;
            libName = "2";
            compName = "Logisim 1.0 D Flip-Flop";
        } else if (key.equals("CirComponentJKFlipFlop")) {
            usesLegacy = true;
            libName = "2";
            compName = "Logisim 1.0 J-K Flip-Flop";
        } else if (key.equals("CirComponentRegister")) {
            usesLegacy = true;
            libName = "2";
            compName = "Logisim 1.0 Register";
        } else if (key.equals("CirComponentRAM")) {
            usesRam = true;
            usesMemory = true;
            libName = "3";
            compName = "RAM";
        } else if (key.equals("CirComponentConstant")) {
            libName = "1";
            compName = "Constant";
            handler = new ConstantHandler();
        } else if (key.equals("CirComponentTristate")) {
            libName ="1";
            compName = "Controlled Buffer";
        } else if (key.equals("CirComponentClock")) {
            libName = "0";
            compName = "Clock";
            addAttribute(contents, "facing", "east");
            handler = new ClockHandler();
        } else if (in.atGroupStart()) {
            throw new IOException("unrecognized key " + key);
        }

        String loc = null;
        in.startGroup();
        while (!in.atGroupEnd()) {
            String attribKey = in.readLine().trim();
            if (attribKey != null && attribKey.length() > 0) {
                if (attribKey.equals("coord")) {
                    String value = in.readGroup();
                    int sep = value.indexOf(' ');
                    if (sep < 0) throw new IOException("Missing loc");
                    try {
                        int x = Integer.parseInt(value.substring(0, sep));
                        int y = Integer.parseInt(value.substring(sep + 1));
                        loc = x + "," + y;
                    } catch (NumberFormatException e) {
                        throw new IOException("Nonnumeric argument");
                    }
                } else {
                    handler.handleKey(attribKey, in, contents);
                }
            }
            if (in.atGroupStart()) {
                in.startGroup();
                while (!in.atGroupEnd()) in.readLine();
                in.endGroup();
            }
        }
        in.endGroup();

        if (loc == null) throw new IOException("component location undefined");
        out.println("  <comp lib=\"" + libName + "\" name=\""
            + compName + "\" loc=\"" + loc + "\">");
        out.print(contentsSource.toString());
        out.println("  </comp>");
    }

    private void handleWireTree(GroupedReader in, PrintWriter out)
            throws IOException {
        in.startGroup();
        while (!in.atGroupEnd()) {
            String key = in.readLine().trim();
            if (key.equals("wire")) {
                in.startGroup();
                String wireAttr = in.readLine().trim();
                if (wireAttr.equals("coords")) {
                    in.startGroup();
                    wireAttr = in.readLine();
                    in.endGroup();
                }
                in.endGroup();
                handleWire(wireAttr, out);
            } else {
                throw new IOException("unrecognized key `" + key + "'");
            }
        }
        in.endGroup();
    }

    private void handleWire(String coords, PrintWriter out)
            throws IOException {
        StringTokenizer tokens = new StringTokenizer(coords);
        try {
            int start_x = Integer.parseInt(tokens.nextToken());
            int start_y = Integer.parseInt(tokens.nextToken());
            int end_x = Integer.parseInt(tokens.nextToken());
            int end_y = Integer.parseInt(tokens.nextToken());
            if (end_x < start_x) {
                int x = start_x; start_x = end_x; end_x = x;
            }
            if (end_y < start_y) {
                int y = start_y; start_y = end_y; end_y = y;
            }
            out.println("  <wire from=\"" + start_x + "," + start_y
                + "\" to=\"" + end_x + "," + end_y + "\" />");
        } catch (java.util.NoSuchElementException e) {
            throw new IOException("Insufficient arguments");
        } catch (NumberFormatException e) {
            throw new IOException("Bad argument");
        }
    }

    private void handleSubcircuit(GroupedReader in, PrintWriter out)
            throws IOException {
        String circName = null;
        String loc = null;
        in.startGroup();
        while (!in.atGroupEnd()) {
            String key = in.readLine().trim();
            if (key.equals("subcircuit")) {
                in.startGroup();
                circName = in.readLine().trim();
                in.endGroup();
            } else if (key.equals("coord")) {
                String value = in.readGroup();
                int sep = value.indexOf(' ');
                if (sep < 0) throw new IOException("Missing loc");
                try {
                    int x = Integer.parseInt(value.substring(0, sep));
                    int y = Integer.parseInt(value.substring(sep + 1));
                    loc = x + "," + y;
                } catch (NumberFormatException e) {
                    throw new IOException("Nonnumeric argument");
                }
            } else {
                throw new IOException("unrecognized key `" + key + "'");
            }
        }
        in.endGroup();

        if (circName == null) throw new IOException("missing subcircuit name");
        if (loc == null) throw new IOException("missing subcircuit location");
        out.println("  <comp name=\"" + circName + "\" loc=\""
            + loc + "\" />");
    }

    private static void addAttribute(PrintWriter dest, String name,
            String value) {
        dest.println("   <a name=\"" + name + "\" val=\"" + value
            + "\" />");
    }

    public static void translate(Reader reader, Writer writer)
            throws IOException {
        new Version1Support(reader, writer).translate();
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("usage: java Version1Support inFile outFile"); //OK
            return;
        }

        File fin = new File(args[0]);
        FileReader reader;
        try {
            reader = new FileReader(fin);
        } catch (IOException e) {
            System.out.println("could not open file: " + e); //OK
            e.printStackTrace();
            return;
        }

        File fout = new File(args[1]);
        FileWriter writer;
        try {
            writer = new FileWriter(fout);
        } catch (IOException e) {
            System.out.println("could not open file: " + e); //OK
            e.printStackTrace();
            return;
        }

        try {
            translate(reader, writer);
            reader.close();
            writer.close();
        } catch (IOException e) {
            System.out.println("aborted due to I/O exception: " + e); //OK
            return;
        }
        System.out.println("completed normally"); //OK
    }
}
