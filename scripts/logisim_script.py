import os.path
import sys

def get_svn_dir(*subdirs):
	script_dir = sys.path[0]
	cur_dir = os.path.dirname(script_dir)
	for subdir in subdirs:
		cur_dir = os.path.join(cur_dir, subdir)
	return cur_dir

def is_same_file(a, b):
	samefile = getattr(os.path, 'samefile', None)
	if samefile is None:
		a_norm = os.path.normcase(os.path.abspath(a))
		b_norm = os.path.normcase(os.path.abspath(b))
		return a_norm == b_norm
	else:
		return samefile(a, b)