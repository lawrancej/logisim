#!/usr/bin/python

import os
import platform
import sys
import shutil
import time
import copy_doc
from logisim_script import *

src_dir = get_svn_dir('src')
doc_dir = get_svn_dir('doc')
libs_dir = get_svn_dir('libs')
data_dir = get_svn_dir('scripts/data')
dest_dir = os.getcwd()
create_jar_start_time = time.time()

# configure defaults
include_source = True
include_documentation = True
bin_dir = None
dest_dir = os.getcwd()
temp_dir = None
keep_temp = False
jdk_dir = None

# see if we're on Carl Burch's platform, and if so reconfigure defaults
if '\\home\\burch\\' in get_svn_dir():
	svn_dir = get_svn_dir()
	home = svn_dir[:svn_dir.find('\\home\\burch\\') + 11]
	bin_dir = build_path(home, 'logisim/TrunkSource/bin')
	dest_dir = build_path(home, 'logisim/Scripts')
	keep_temp = False

usage = '''usage: create-jar ARGS
arguments:  -bin DIR     compiled files are already present in DIR
            -d DIR       place created JAR file in DIR
            -j DIR       directory for JDK binaries (for java, javac, jar)
            -keep        keep temporary directory after completion
            -nodoc       omit documentation from created JAR file
            -nosrc       omit source code from created JAR file
            -temp DIR    use DIR as the temporary directory
'''.rstrip()

argi = 0
print_usage = False
while argi + 1 < len(sys.argv):
	argi += 1
	arg = sys.argv[argi]
	if arg == '-bin' and argi + 1 < len(sys.argv):
		argi += 1
		bin_dir = sys.argv[argi]
	elif arg == '-d' and argi + 1 < len(sys.argv):
		argi += 1
		dest_dir = sys.argv[argi]
	elif arg == '-j' and argi + 1 < len(sys.argv):
		argi += 1
		jdk_dir = sys.argv[argi]
	elif arg == '-keep':
		keep_temp = True  
	elif arg == '-nodoc':
		include_documentation = False
	elif arg == '-nosrc':
		include_source = False
	elif arg == '-temp' and argi + 1 < len(sys.argv):
		argi += 1
		temp_dir = sys.argv[argi]
	else:
		print_usage = True
if print_usage:
	sys.exit(usage)

if bin_dir is not None and not os.path.exists(bin_dir):
	sys.exit('binary directory ' + bin_dir + ' does not exist')
if not os.path.exists(dest_dir):
	sys.exit('destination directory ' + dest_dir + ' does not exist')
if temp_dir is not None and not os.path.exists(os.path.dirname(temp_dir)):
	sys.exit('temporary directory\'s parent ' + os.path.dirname(temp_dir)
			+ ' does not exist')
if jdk_dir is None:
	jar_exec = 'jar'
	java_exec = 'java'
	javac_exec = 'javac'
elif os.path.exists(jdk_dir):
	for file in ['jar', 'java', 'javac']:
		file_path = build_path(jdk_dir, 'bin', file)
		if not os.path.exists(file_path):
			file_path += '.exe'  # see if it exists with .exe extension
			if not os.path.exists(file_path):
				sys.exit('could not find \'' + file + '\' executable in JDK '
						+ 'directory ' + jdk_dir)
		file_path = '"' + uncygwin(file_path) + '"'
		if file == 'jar': jar_exec = file_path
		elif file == 'java': java_exec = file_path
		else: javac_exec = file_path
else:
	sys.exit('JDK directory ' + jdk_dir + ' does not exist')

#
# Determine the version number and copyright year
#
version, copyright = determine_version_info()
if version is None or copyright is None:
	sys.exit(-1)
print('version ' + version + ', (c) ' + copyright)
	
if bin_dir is None:
	print('warning: no "-bin" argument provided - will have to compile all sources')

#
# prepare the temporary directory to be completely empty
#
if temp_dir is None:
	current = os.listdir(dest_dir)
	for i in range(1, 11):
		name = 'files' if i is 1 else 'files' + str(i)
		if name not in current:
			temp_dir = os.path.join(dest_dir, name)
			break
	if temp_dir is None:
		sys.exit('all default temporary directories (files* in ' + dest_dir
				+ ') already exist, please clean')
	print('using temporary directory ' + temp_dir)
elif os.path.exists(temp_dir):
	print('Temporary directory ' + temp_dir + ' already exists.')
	user = prompt('Do you want to replace it ([y]es, [n]o)?', 'yn')
	if user == 'n':
		sys.exit()
	shutil.rmtree(temp_dir)
os.mkdir(temp_dir)

# from here to the end, any fatal errors should first delete the temporary
# directory - we set this up so this happens automatically
old_sys_exit = sys.exit
def fatal_error(msg=None):
	if not keep_temp:
		os.chdir(dest_dir)
		shutil.rmtree(temp_dir)
	if msg is None:
		old_sys_exit(-1)
	else:
		old_sys_exit(msg)
sys.exit = fatal_error

# set things up so dot-files are excluded when copying directories
def copytree(src_dir, dst_dir, ignore=None):
	def ignore_hidden(dir, files):
		ret = [] if ignore is None else ignore(dir, files)
		ret.extend([f for f in files if f.startswith('.') and f not in ret])
		return ret
	shutil.copytree(src_dir, dst_dir, ignore=ignore_hidden)

#
# copy the binary directory if provided
#
# first determine which JAR files we're using
jar_files = [f for f in os.listdir(libs_dir) if f.endswith('.jar')]
# now get the .class files
if bin_dir is None:
	print('compiling .class files - this may take a full minute')
	classpath = []
	for jar_file in jar_files:
		classpath.append(build_path(libs_dir, jar_file, cygwin=False))
	syst = platform.system().upper()
	if syst.startswith("CYGWIN"):
		classpath = '"' + ';'.join(classpath) + '"'
	elif syst.startswith('WINDOWS'):
		classpath = ';'.join(classpath)
	else:
		classpath = ':'.join(classpath)
	os.chdir(src_dir)
	java_files_path = build_path(temp_dir, 'source_files')
	with open(java_files_path, 'w') as java_files:
		for path, dirs, files in os.walk('.'):
			if '.svn' in dirs:
				dirs.remove('.svn')
			for file in files:
				if file.endswith('.java'):
					if path.startswith(src_dir):
						file_path = build_path(path[len(src_dir) + 1:], file)
					else:
						file_path = build_path(path, file)
					java_files.write(uncygwin(file_path) + '\n')
	javac_args = [javac_exec, '-source', '1.5', '-target', '1.5',
				'-d', uncygwin(temp_dir), '-classpath', classpath,
				'@' + uncygwin(java_files_path, verbose=True)]
	before_compile = time.time()
	exit_code = system(*javac_args)
	elapse = format(time.time() - before_compile, '.1f')
	print('   (' + elapse + 's elapsed during compile)')
	os.remove(java_files_path)
	if exit_code != 0:
		fatal_error('error during compilation, JAR creation script aborted')
else:
	print('copying .class files')
	copytree(build_path(bin_dir, 'com'), build_path(temp_dir, 'com'))

#
# unpack all the JARs being used
#
print('unpacking JAR libraries')
os.chdir(temp_dir)
if not include_documentation and 'jh.jar' in jar_files:
	jar_files.remove('jh.jar')
for jar_file in jar_files:
	system(jar_exec, 'fx', build_path(libs_dir, jar_file, cygwin=False))
# remove any directories from JARs that will be used for Logisim's purposes
for exclude in ['src', 'resources', 'META-INF', 'COPYING.TXT']:
	if os.path.exists(build_path(temp_dir, exclude)):
		shutil.rmtree(build_path(temp_dir, exclude))


#
# create the resources, src, and documentation directories
#
print('copying resources')
copytree(build_path(src_dir, 'resources'), build_path(temp_dir, 'resources'))

if include_source:
	print('copying source code')
	os.mkdir(build_path(temp_dir, 'src'))
	copytree(build_path(src_dir, 'com'), build_path(temp_dir, 'src/com'))
	
if include_documentation:
	print('copying documentation')
	doc_dst = build_path(temp_dir, 'doc')
	copy_doc.copy_files_all_locales(doc_dir, doc_dst)
	copy_doc.build_contents(doc_dir, doc_dst)
	copy_doc.build_map(doc_dir, doc_dst)
	copy_doc.build_helpset(doc_dir, doc_dst)
	
	jhindexer = build_path(data_dir, 'javahelp/bin/jhindexer.jar', cygwin=False)
	os.chdir(doc_dst)
	for locale in os.listdir(doc_dst):
		locale_dst = build_path(doc_dst, locale)
		if os.path.isdir(locale_dst):
			cmd_args = [java_exec, '-jar', jhindexer]
			if os.path.exists('jhindexer.cfg'):
				cmd_args.extend(['-c', 'jhindexer.cfg'])
			cmd_args.extend(['-db', 'search_lookup_' + locale])
			cmd_args.extend(['-locale', locale])
			found = False
			for sub in [locale + '/html/guide', locale + '/html/libs']:
				if os.path.exists(build_path(doc_dst, sub)):
					cmd_args.append(sub)
					found = True
			if found:
				print('indexing documentation [' + locale + ']')
				system(*cmd_args)

#
# copy in the .class files that were compiled under MacOS
#
print('adding MacOS support')
macos_dir = build_path(data_dir, 'macos-classes')
for macos_file in os.listdir(macos_dir):
	if macos_file.endswith('.class'):
		macos_dst = build_path(temp_dir, 'com/cburch/logisim/gui/start', macos_file)
		shutil.copyfile(build_path(macos_dir, macos_file), macos_dst)

#
# insert GPL, create manifest
#
print('creating JAR file')
# include COPYING.TXT and manifest
shutil.copyfile(build_path(data_dir, 'COPYING.TXT'),
			build_path(temp_dir, 'COPYING.TXT'))
os.mkdir(build_path(temp_dir, 'META-INF'))
with open(build_path(temp_dir, 'META-INF/MANIFEST.MF'), 'w') as manifest:
	manifest.write('Main-Class: com.cburch.logisim.Main\n')
# and now create the JAR
os.chdir(temp_dir)
files_to_include = os.listdir('.')
files_to_include.remove('META-INF')
jar_filename = 'logisim-fragile-' + version + '.jar'
jar_pathname = build_path(dest_dir, jar_filename, cygwin=False)
system(jar_exec, 'fcm', jar_pathname, 'META-INF/MANIFEST.MF', *files_to_include)

os.chdir(dest_dir)
if not keep_temp:
	print('deleting temporary directory')
	shutil.rmtree(temp_dir)
size = str(int(round(os.path.getsize(jar_pathname) / 1024.0)))
elapse = format(time.time() - create_jar_start_time, '.1f')
summary = 'JAR file created! (name: {name}, size: {size}KB, elapsed: {elapse}s)'
print(summary.format(name=jar_filename, size=str(size), elapse=elapse))