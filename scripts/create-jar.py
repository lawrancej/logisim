#!/cygdrive/c/Python31/python

import os
import re
import sys
import shutil
import glob
from logisim_script import *

src_dir = get_svn_dir('src')
doc_dir = get_svn_dir('doc')
libs_dir = get_svn_dir('libs')
data_dir = get_svn_dir('scripts/data')
dest_dir = os.getcwd()

jar_exec = 'jar'
javac_exec = 'javac'

# configure defaults
include_source = True
include_documentation = True
bin_dir = None
dest_dir = os.getcwd()
temp_dir = None
keep_temp = False

# see if we're on Carl Burch's platform, and if so reconfigure defaults
if '\\home\\burch\\' in get_svn_dir():
	svn_dir = get_svn_dir()
	home = svn_dir[:svn_dir.find('\\home\\burch\\') + 11]
	bin_dir = build_path(home, 'logisim/TrunkSource/bin')
	dest_dir = build_path(home, 'logisim/Scripts')

usage = '''usage: create-jar ARGS
arguments:  -bin DIR     compiled files are already present in DIR
            -d DIR       place created JAR file in DIR
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
		bin_dir = argv[argi]
	elif arg == '-d' and argi + 1 < len(sys.argv):
		argi += 1
		dest_dir = argv[argi]
	elif arg == '-keep':
		keep_temp = True  
	elif arg == '-nodoc':
		include_documentation = False
	elif arg == '-nosrc':
		include_source = False
	elif arg == '-temp' and argi + 1 < len(sys.argv):
		argi += 1
		temp_dir = argv[argi]
	else:
		print_usage = True
if print_usage:
	sys.exit(usage)
	
if bin_dir is None:
	sys.exit('automatic compilation of source code is not yet supported')

if bin_dir is not None and not os.path.exists(bin_dir):
	sys.exit('binary directory ' + bin_dir + ' does not exist')
if not os.path.exists(dest_dir):
	sys.exit('destination directory ' + dest_dir + ' does not exist')
if temp_dir is not None and not os.path.exists(os.path.dirname(temp_dir)):
	sys.exit('temporary directory\'s parent ' + os.path.dirname(temp_dir)
			+ ' does not exist')

#
# Determine the version number and copyright year
#
version, copyright = determine_version_info()
if version is None or copyright is None:
	sys.exit(-1)
print('version ' + version + ', (c) ' + copyright)

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
if bin_dir is not None:
	print('copying binary files')
	copytree(build_path(bin_dir, 'com'), build_path(temp_dir, 'com'))

#
# unpack all the JARs being used
#
print('unpacking JAR libraries')
os.chdir(temp_dir)
for jar_file in os.listdir(libs_dir):
	if jar_file.endswith('.jar'):
		if jar_file == 'jh.jar' and not include_documentation:
			pass # we don't want to include documentation here
		else:
			system(jar_exec, 'fx', build_path(libs_dir, jar_file))
# don't include directories that will be created anyway for JAR file
for exclude in ['src', 'resources', 'META-INF', 'COPYING.TXT']:
	if os.path.exists(build_path(temp_dir, exclude)):
		shutil.rmtree(build_path(temp_dir, exclude))

#
# compile the binary directory if it wasn't provided
#
if bin_dir is None:
	print('compiling source code')
	sys.exit('not yet supported')

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
	copytree(doc_dir, build_path(temp_dir, 'doc'))
	
	jhindexer = build_path(data_dir, 'javahelp/bin/jhindexer.jar')
	for locale in os.listdir(doc_dir):
		locale_dir = build_path(temp_dir, 'doc', locale)
		if os.path.isdir(locale_dir):
			print('indexing documentation [' + locale + ']')
			os.chdir(locale_dir)
			cmd_args = ['java', '-jar', jhindexer]
			if os.path.exists('jhindexer.cfg'):
				cmd_args.extend(['-c', 'jhindexer.cfg'])
			cmd_args.extend(['-locale', locale])
			cmd_args.extend(['guide', 'libs'])
			system(*cmd_args)

#
# copy in the .class files that were compiled under MacOS
#
print('adding MacOS support')
macos_dir = build_path(data_dir, 'macos-classes')
for macos_file in os.listdir(macos_dir):
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
with open(build_path(temp_dir, 'META-INF/MANIFEST.MF'), 'wb') as manifest:
	manifest.write(bytes('Main-Class: com.cburch.logisim.Main\n', 'UTF-8'))
# and now create the JAR
os.chdir(temp_dir)
files_to_include = os.listdir('.')
files_to_include.remove('META-INF')
jar_filename = 'logisim-fragile-' + version + '.jar'
jar_pathname = build_path(dest_dir, jar_filename)
system(jar_exec, 'fcm', jar_pathname, 'META-INF/MANIFEST.MF', *files_to_include)

if not keep_temp:
	print('deleting temporary directory')
	shutil.rmtree(temp_dir)
size = int(round(os.path.getsize(jar_pathname) / 1024.0))
print('done creating ' + jar_filename + ' (' + str(size) + 'KB)')