#!/usr/bin/python

import os
import re
import shutil
import sys
import xml.dom
import xml.dom.minidom
from logisim_script import *

lang = 'en'
if len(sys.argv) > 1:
	lang = sys.argv[1]

# see if we're on Carl Burch's platform, and if so reconfigure defaults
if '\\home\\burch\\' in get_svn_dir():
	lang = 'en'
	svn_dir = get_svn_dir()
	home = svn_dir[:svn_dir.find('\\home\\burch\\') + 11]
	lang_path = build_path(home, 'logisim/Scripts/www/docs/2.6.0', lang)
	if not os.path.exists(lang_path):
		os.mkdir(lang_path)
	os.chdir(lang_path)

head_end = r'''
<link rel="shortcut icon" href="{rel}/../../../logisim.ico" />
<link rel="stylesheet" type="text/css" href="{rel}/../../docstyle.css" />
<link rel="stylesheet" type="text/css" href="{rel}/../../tree/simpletree.css" /> 
<script type="text/javascript" src="{rel}/../../tree/simpletreemenu.js">

/***********************************************
* Simple Tree Menu- © Dynamic Drive DHTML code library (www.dynamicdrive.com)
* This notice MUST stay intact for legal use
* Visit Dynamic Drive at http://www.dynamicdrive.com/ for full source code
***********************************************/

// (from http://www.dynamicdrive.com/dynamicindex1/navigate1.htm)
</script>
'''.strip()

body_start = r'''
<div id="content">
'''.strip()

body_end = r'''
</div>

<div id="map">
<a href="{rel}/../../../{lang}index.html"><img src="{rel}/../../../{lang}header.png"
    border="0" width="227" height="137"></a>
<ul id="maptree" class="treeview">
{map}
</ul>
</div>
<script type="text/javascript"><!--
ddtreemenu.setPath('{rel}/../../tree');
ddtreemenu.createTree("maptree");
// --></script>
'''.strip()

#
# determine the source and destination directories
#
dst_dir = os.getcwd()
src_dir = get_svn_dir('doc', lang)
if not os.path.exists(src_dir):
	sys.exit('source directory doc/{lang} not found, aborted'.format(lang=lang))
if is_same_file(dst_dir, src_dir) or os.path.exists(os.path.join(dst_dir, 'doc.hs')):
	sys.exit('cannot place result into source directory')

#
# load the nodes from map.jhm
#	
map_target = { }
map_url = { }
class MapNode():
	def __init__(self, target, url):
		self.target = target
		self.url = url

map_src = os.path.join(src_dir, 'map.jhm')
if not os.path.exists(map_src):
	map_src = os.path.join(os.path.dirname(src_dir), 'en')
	map_src = os.path.join(map_src, 'map.jhm')
map_dom = xml.dom.minidom.parse(map_src)
for mapid in map_dom.getElementsByTagName('mapID'):
	if not mapid.hasAttribute('target') or not mapid.hasAttribute('url'):
		print('node is missing target or url attribute, ignored')
		continue
	target = mapid.getAttribute('target')
	url = mapid.getAttribute('url')
	node = MapNode(target, url)
	map_target[target] = node
	map_url[url] = node
map_dom = None

#
# determine the table of contents
#
toc_nodes = []
def walk_contents(node, ancestors):
	ret = []
	for child in node.childNodes:
		if getattr(child, 'tagName', '-') != 'tocitem':
			ret.extend(walk_contents(child, ancestors))
		elif not child.hasAttribute('target'):
			print('contents.xml: node is missing target')
			ret.extend(walk_contents(child, ancestors))
		elif child.getAttribute('target') not in map_target:
			print('contents.xml: unknown target ' + target)
			ret.extend(walk_contents(child, ancestors))
		else:
			map_node = map_target[child.getAttribute('target')]
			map_node.ancestors = ancestors
			map_node.text = child.getAttribute('text')
			toc_nodes.append(map_node)
			ret.append(map_node)
			map_node.children = walk_contents(child, (map_node, ) + ancestors)
	return ret
contents_dom = xml.dom.minidom.parse(os.path.join(src_dir, 'contents.xml'))
walk_contents(contents_dom.documentElement, ())

#
# determine the files to be replaced
#
to_mkdir = [] # of directory pathnames
to_copy = [] # of names for regular files
for root, dirs, files in os.walk(src_dir):
	rel_root = os.path.relpath(root, src_dir)
	dirs.remove('.svn')
	for dir in dirs:
		to_mkdir.append(os.path.join(rel_root, dir))
	for file in files:
		if file not in ['contents.xml', 'doc.hs', 'jhindexer.cfg', 'map.jhm']:
			to_copy.append(os.path.join(rel_root, file))
	
delete_from_copy = []
delete_rest = False
for file in to_copy:
	dst_file = os.path.join(dst_dir, file)
	if os.path.exists(dst_file):
		if delete_rest:
			delete_from_copy.append(file)
		else:
			print('{file} already exists.'.format(file=file))
			options = 'Replace ([y]es, [Y]es to all, [n]o, [N]o to all, [a]bort'
			dispose = prompt('Replace (' + options + ')?', 'yYnNa')
			if dispose == 'y':
				pass
			elif dispose == 'Y':
				break
			elif dispose == 'n':
				delete_from_copy.append(file)
			elif dispose == 'N':
				delete_from_copy.append(file)
				delete_rest = True
			elif dispose == 'a':
				sys.exit('aborted on user request') 

for file in delete_from_copy:
	to_copy.remove(file)

if len(to_copy) == 0:
	print('nothing to copy')
	sys.exit()
	
#
# Create the directories and copy over the non-HTML files
#
print('creating directories and copying non-HTML files')
for dir in to_mkdir:
	path = os.path.join(dst_dir, dir)
	if not os.path.exists(path):
		os.mkdir(path)
for file in to_copy:
	if not file.endswith('.html'):
		shutil.copyfile(os.path.join(src_dir, file), os.path.join(dst_dir, file))

#
# Create the HTML files
#
print('creating HTML files')
if lang == 'en':
	url_lang = ''
else:
	url_lang = lang + '/'
def create_map(filename):
	dst_filename = os.path.join(dst_dir, filename)
	url = filename.replace('\\', '/')
	if url in map_url:
		ancestors = getattr(map_url[url], 'ancestors', ())
	else:
		ancestors = ()
	map_lines = []
	cur_depth = 1
	for node in toc_nodes:
		depth = len(node.ancestors)
		if depth == 0: # ignore the root node
			continue
		
		while cur_depth > depth:
			cur_depth -= 1
			map_lines.append('  ' * cur_depth + '</ul></li>')
		
		if node.url == url:
			text_fmt = '<b{text_clss}>{text}</b>'
		else:
			text_fmt = '<a href="{arel}"{text_clss}>{text}</a>'
		node_filename = os.path.join(dst_dir, node.url)
		arel = os.path.relpath(node_filename, os.path.dirname(dst_filename))
		arel = arel.replace('\\', '/')
		if node in ancestors or node.url == url:
			text_clss = ' class="onpath"'
		else:
			text_clss = ' class="offpath"'
		entry = text_fmt.format(arel=arel, text_clss=text_clss, text=node.text)
		if len(node.children) == 0:
			map_lines.append('  ' * cur_depth + '<li>' + entry + '</li>')
		else:
			if node in ancestors or node.url == url:
				ul = '<ul rel="open">'
			else:
				ul = '<ul>'
			map_lines.append('  ' * cur_depth + '<li>' + entry + ul)
			cur_depth += 1
	while cur_depth > 1:
		cur_depth -= 1
		map_lines.append('  ' * cur_depth + '</ul></li>')
	return '\n'.join(map_lines)
	
body_start_re = re.compile('<body[^>]*>')
for filename in (f for f in to_copy if f.endswith('.html')):
	src_filename = os.path.join(src_dir, filename)
	dst_filename = os.path.join(dst_dir, filename)
	map = create_map(filename)
	with open(src_filename, 'r', encoding='utf-8') as src_f:
		rel = os.path.relpath(dst_dir, os.path.dirname(dst_filename))
		rel = rel.replace('\\', '/')
		
		text = src_f.read();
		
		head_end_pos = text.find('</head>')
		if head_end_pos >= 0:
			to_head = head_end.format(rel=rel, lang=url_lang)
			text = text[:head_end_pos] + to_head + '\n' + text[head_end_pos:]
			
		body_start_match = body_start_re.search(text)
		if body_start_match:
			body_start_pos = body_start_match.end()
			to_body = body_start.format(rel=rel, map=map, lang=url_lang)
			text = text[:body_start_pos] + '\n' + to_body + text[body_start_pos:]
			
		body_end_pos = text.find('</body>')
		if body_end_pos >= 0:
			to_body = body_end.format(rel=rel, map=map, lang=url_lang)
			text = text[:body_end_pos] + to_body + '\n' + text[body_end_pos:]
			
		with open(dst_filename, 'w', encoding='utf-8') as dst_f:
			dst_f.write(text) 
	
print('file generation complete')