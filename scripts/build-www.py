#!/usr/bin/python

import os
import re
import shutil
import sys
import xml.dom
import xml.dom.minidom
from logisim_script import *

dst_path = os.getcwd()
src_path = build_path(get_svn_dir(), 'www')

# see if we're on Carl Burch's platform, and if so reconfigure defaults
if '\\home\\burch\\' in get_svn_dir():
	svn_dir = get_svn_dir()
	home = svn_dir[:svn_dir.find('\\home\\burch\\') + 11]
	dst_path = build_path(home, 'logisim/Scripts/www/base')
	if not os.path.exists(dst_path):
		os.mkdir(dst_path)
	os.chdir(dst_path)

# determine which HTML files are represented
print('determining HTML files available')
html_files = { }
languages = []
for lang in os.listdir(src_path):
	lang_dir = build_path(src_path, lang)
	if lang not in ['en', '.svn'] and os.path.isdir(build_path(src_path, lang)):
		actual = lang
		if actual == 'root':
			actual = 'en'
		languages.append((actual, lang_dir))
		for file in os.listdir(lang_dir):
			if file.endswith('.html'):
				if file not in html_files:
					html_files[file] = []
				html_files[file].append(actual)
del html_files['template.html']
for file in html_files:
	html_files[file].sort()

# define function for creating translation menu for a file
template_langmenu_other = '''<tr>
    <th><a href="{rel}{xndir}{file}">[{xn}]</a></th>
    <td><a href="{rel}{xndir}{file}">{xnname}</a></td>
</tr>'''
template_langmenu_cur = '''<tr><th>[{xn}]</th><td>{xnname}</td></tr>'''
langmenu_names = {
	'de': 'Deutsch',
	'el': '\u0395\u03BB\u03BB\u03B7\u03BD\u03B9\u03BA\u03AC',
	'en': 'English',
	'es': 'espa\u00f1ol',
	'pt': 'Portugu\u00eas',
	'ru': '\u0420\u0443\u0441\u0441\u043a\u0438\u0439',
}

def build_langmenu(file, lang):
	if file in html_files and len(html_files[file]) > 1:
		ret = ['<div class="langmenu"><table><tbody>']
		rel = '../'
		if lang == 'en':
			rel = ''
		for xn in html_files[file]:
			if xn == 'en':
				xndir = ''
			else:
				xndir = xn + '/'
			if xn == lang:
				template = template_langmenu_cur
			else:
				template = template_langmenu_other
			xnname = langmenu_names.get(xn, '???')
			ret.append(template.format(xn=xn, xndir=xndir, rel=rel, file=file,
									xnname=xnname))
		ret.append('</tbody></table></div>')
		return '\n'.join(ret)
	else:
		return ''

# Now go through each language directory
template_head_re = re.compile(r'<head>\s*<meta http-equiv="content-type[^>]*>\s*(\S.*)</head>', re.MULTILINE | re.DOTALL)
template_body_re = re.compile(r'<body>(.*)</body>', re.MULTILINE | re.DOTALL)
file_head_re = re.compile(r'</head>')
file_body_re = re.compile(r'<body>(.*)</body>', re.MULTILINE | re.DOTALL)
repl_langmenu_re = re.compile(r'<langmenu\s*/?>', re.MULTILINE)
repl_contents_re = re.compile(r'<contents\s*/?>', re.MULTILINE)
for lang, lang_src in languages:
	print('building directory for ' + lang)
	# load the template
	template_path = build_path(lang_src, 'template.html')
	template_head = ''
	template_body = '<contents />'
	if os.path.exists(template_path):
		with open(template_path, encoding='utf-8') as template_file:
			template_text = template_file.read()
		template_head_match = template_head_re.search(template_text)
		if template_head_match is None: print('  ' + lang + ' template: no head' + str(len(template_text)))
		if template_head_match is not None:
			template_head = template_head_match.group(1)
		template_body_match = template_body_re.search(template_text)
		if template_body_match is None: print('  ' + lang + ' template: no body' + str(len(template_text)))
		if template_body_match is not None:
			template_body = template_body_match.group(1)
		
	# determine the destination directory
	if lang == 'en':
		lang_dst = dst_path
	else:
		lang_dst = build_path(dst_path, lang)
		if not os.path.exists(lang_dst) or os.path.getsize(lang_dst) == 0:
			os.mkdir(lang_dst)
	
	# copy each file over
	for file in os.listdir(lang_src):
		file_ext_start = file.rindex('.')
		if file_ext_start >= 0:
			file_ext = file[file_ext_start + 1:]
		file_src = build_path(lang_src, file)
		file_dst = build_path(lang_dst, file)
		if file_ext in ['png', 'css', 'ico']:
			shutil.copy(file_src, file_dst)
		elif file_ext == 'html' and file != 'template.html':
			with open(file_src, encoding='utf-8') as html_file:
				html_text = html_file.read()
			html_text = file_head_re.sub(template_head + '</head>', html_text)
			html_body_match = file_body_re.search(html_text)
			if  html_body_match is None:
				html_body = ''
			else:
				html_body = html_body_match.group(1)
				body_start = html_body_match.start()
				body_end = html_body_match.end()
				html_text = (html_text[:body_start] + '<body>' + template_body
							+ '</body>' + html_text[body_end:])
			langmenu = build_langmenu(file, lang)
			html_text = repl_langmenu_re.sub(langmenu, html_text)
			html_text = repl_contents_re.sub(html_body, html_text)
			with open(file_dst, 'w', encoding='utf-8') as html_file:
				html_file.write(html_text)

print('HTML generation complete')