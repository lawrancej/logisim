#!/usr/bin/python

import os
import re
import shutil
import sys
import xml.dom
import xml.dom.minidom
import copy_doc
from logisim_script import *

src_dir = get_svn_dir('doc')
dst_dir = os.getcwd()

# see if we're on Carl Burch's platform, and if so reconfigure defaults
if '\\home\\burch\\' in get_svn_dir():
	svn_dir = get_svn_dir()
	home = svn_dir[:svn_dir.find('\\home\\burch\\') + 11]
	dst_dir = build_path(home, 'logisim/Scripts/www/docs')

head_end = r'''
<link rel="shortcut icon" href="{rel}/../../../logisim.ico" />
<link rel="stylesheet" type="text/css" href="{rel}/../docstyle.css" />
<link rel="stylesheet" type="text/css" href="{rel}/../simpletree.css" /> 
<script type="text/javascript" src="{rel}/../simpletreemenu.js">

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
<a href="{langhome}/index.html"><img src="{langhome}/header.png"
    border="0" width="227" height="137"></a>
<ul id="maptree" class="treeview">
{map}
</ul>
</div>
<script type="text/javascript"><!--
ddtreemenu.setPath('{rel}/..');
ddtreemenu.createTree("maptree");
// --></script>
'''.strip()

#
# determine the source and destination directories
#
if not os.path.exists(src_dir):
	sys.exit('source directory doc/{lang} not found, aborted'.format(lang=lang))
if not os.path.exists(dst_dir):
	sys.exit('destination directory not found, aborted')
if is_same_file(dst_dir, src_dir) or os.path.exists(os.path.join(dst_dir, 'doc.hs')):
	sys.exit('cannot place result into source directory')

#
# deal with replacing files
#
replace_all = 0  # 0 unknown, 1 yes to all, -1 no to all
def confirm_replace(filename):
	global replace_all
	if replace_all == 0:
		print('{file} already exists.'.format(file=filename))
		options = '[y]es, [Y]es to all, [n]o, [N]o to all, [a]bort'
		dispose = prompt('Replace (' + options + ')?', 'yYnNa')
		if dispose == 'y':
			return True
		elif dispose == 'Y':
			replace_all = 1
			return True
		elif dispose == 'n':
			return False
		elif dispose == 'N':
			replace_all = -1
			return False
		elif dispose == 'a':
			sys.exit('aborted on user request')
	else:
		return replace_all >= 0

# create the support directory
support_src = build_path(src_dir, 'support/www')
support_dst = dst_dir
if not os.path.exists(support_dst):
	os.mkdir(support_dst)
for file in os.listdir(support_src):
	if file != '.svn':
		file_dst = build_path(support_dst, file)
		if os.path.exists(file_dst):
			os.remove(file_dst)
		shutil.copy(build_path(support_src, file), file_dst)

# create the directory for each locale
for locale in copy_doc.get_doc_locales(src_dir):
	locale_src = build_path(src_dir, locale)
	locale_dst = build_path(dst_dir, locale)
	if not os.path.exists(locale_dst):
		os.mkdir(locale_dst)
			
	# determine how targets correspond to URLs
	map_target = {}
	map_url = {}
	class MapNode():
		def __init__(self, target, url):
			self.target = target
			self.url = url
	
	map_text = copy_doc.get_map(locale, src_dir)
	map_dom = xml.dom.minidom.parseString(map_text)
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
	
	# determine the table of contents
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
				target = child.getAttribute('target')
				map_node = map_target[target]
				map_node.ancestors = ancestors
				map_node.text = child.getAttribute('text')
				toc_nodes.append(map_node)
				ret.append(map_node)
				map_node.children = walk_contents(child, (map_node, ) + ancestors)
		return ret
	contents_text = copy_doc.get_contents(locale, src_dir)
	contents_dom = xml.dom.minidom.parseString(contents_text)
	walk_contents(contents_dom.documentElement, ())

	# create function for creating contents section corresponding to a filename
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
			
	# create function for translating HTML in source file to HTML in destination
	re_body_start = re.compile('<body[^>]*>')
	re_path_dir = re.compile('[^/]*/')
	def wrap_html(html_text, rel_filename):
		map = create_map(locale + '/' + rel_filename)
		rel_base = re_path_dir.sub('../', rel_filename)
		slash = rel_base.rfind('/')
		if slash >= 0:
			rel_base = rel_base[:slash]
		
		if locale == 'en':
			langhome = rel_base + '/../../..'
		else:
			langhome = rel_base + '/../../../' + locale

		text = html_text
		head_end_pos = text.find('</head>')
		if head_end_pos >= 0:
			to_head = head_end.format(rel=rel_base, lang=locale)
			text = text[:head_end_pos] + to_head + '\n' + text[head_end_pos:]
			
		body_start_match = re_body_start.search(text)
		if body_start_match:
			body_start_pos = body_start_match.end()
			
			to_body = body_start.format(rel=rel_base, map=map, lang=locale,
				langhome=langhome)
			text = text[:body_start_pos] + '\n' + to_body + text[body_start_pos:]
			
		body_end_pos = text.find('</body>')
		if body_end_pos >= 0:
			to_body = body_end.format(rel=rel_base, map=map, lang=locale,
				langhome=langhome)
			text = text[:body_end_pos] + to_body + '\n' + text[body_end_pos:]
			
		return text
	
	# copy the files over
	print('creating files [' + locale + ']')
	copy_doc.copy_files(locale, src_dir, dst_dir, translate_html=wrap_html,
					confirm_replace=confirm_replace)
	
print('file generation complete')