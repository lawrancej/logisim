import os.path
import platform
import re
import sys

def build_path(first, *subdirs, **kwargs):
	dir = first
	for subdir in subdirs:
		for subsub in subdir.split('/'):
			dir = os.path.join(dir, subsub)
	if 'cygwin' in kwargs and not kwargs['cygwin']:
		return uncygwin(dir)
	else:
		return dir

def get_svn_dir(*subdirs):
	script_parent = os.path.dirname(sys.path[0])
	return build_path(script_parent, *subdirs)

def uncygwin(path, verbose=False):
	sys = platform.system()
	if sys.startswith('CYGWIN'):
		if path.startswith('/cygdrive/'):
			path = path[10].upper() + ':' + path[11:]
		elif path.startswith('/'):
			path = 'C:/cygwin' + path
		return path
	else:
		return path 

def is_same_file(a, b):
	samefile = getattr(os.path, 'samefile', None)
	if samefile is None:
		a_norm = os.path.normcase(os.path.abspath(a))
		b_norm = os.path.normcase(os.path.abspath(b))
		return a_norm == b_norm
	else:
		return samefile(a, b)

def system(*args):
	return os.system(' '.join(args))
	
def prompt(prompt, accept=None):
	while True:
		ret = input(prompt + ' ')
		ret = ret.rstrip()
		if accept is None or ret in accept:
			return ret

def determine_version_info():
	version = None
	version_line = -1
	copyright = None
	copyright_line = -1
	main_name = get_svn_dir('src/com/cburch/logisim/Main.java')
	with open(main_name) as main_file:
		version_re = re.compile(r'.*VERSION = LogisimVersion.get\(([0-9, ]*)\)')
		year_re = re.compile(r'COPYRIGHT_YEAR = ([0-9]+)')
		line_num = -1
		for line in main_file:
			line_num += 1
			mat = version_re.search(line)
			if mat:
				if version_line < 0:
					version = re.sub(r'\s*,\s*', '.', mat.group(1))
					version_line = line_num
				else:
					sys.stderr.write('duplicate version on line '
						+ str(version_line) + ' and ' + str(line_num) + '\n')
					version = None
			mat = year_re.search(line)
			if mat:
				if copyright_line < 0:
					copyright = mat.group(1)
					copyright_line = line_num
				else:
					sys.stderr.write('duplicate copyright year on line '
						+ str(copyright_line) + ' and ' + str(line_num) + '\n')
					copyright = None
	if version_line < 0:
		sys.stderr.write('version number not found\n')
	if copyright_line < 0:
		sys.stderr.write('copyright year not found\n')
	return version, copyright
