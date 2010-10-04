#!/usr/bin/python

import datetime
import os
import re
import shutil
import tarfile
from logisim_script import *

FILENAME = 'logisim-pot.tgz'

gui_dir = get_svn_dir('src', 'resources/logisim')
dst_dir = os.path.join(os.getcwd(), 'pot-dir')
dst_tarball = build_path(os.getcwd(), FILENAME)
known_langs = 'ru es'.split()
keep_temporary = False

if '\\home\\burch\\' in get_svn_dir():
    tar_cmd = 'C:/cygwin/bin/tar.exe'
    
if 'unichr' not in dir():
    unichr = chr

if os.path.exists(dst_tarball):
    user = prompt('file ' + FILENAME + ' already exists. Replace [y/n]?', 'yn')
    if user == 'n':
        sys.exit('POT creation aborted on user request')
    os.remove(dst_tarball)

#
# Clear the destination directory
#
if os.path.exists(dst_dir):
    shutil.rmtree(dst_dir)
os.mkdir(dst_dir)

version, copyright = determine_version_info()
date = datetime.datetime.now().strftime('%Y-%m-%d %H:%M%z')

header = r'''
msgid ""
msgstr ""
"Project-Id-Version: Logisim {version}\n"
"Report-Msgid-Bugs-To: \n"
"POT-Creation-Date: {date}\n"
"PO-Revision-Date: YEAR-MO-DA HO:MI+ZONE\n"
"Last-Translator: FULL NAME <EMAIL@ADDRESS>\n"
"Language-Team: LANGUAGE <LL@li.org>\n"
"MIME-Version: 1.0\n"
"Content-Type: text/plain; charset=utf-8\n"
"Content-Transfer-Encoding: 8bit\n"
"Plural-Forms: nplurals=INTEGER; plural=EXPRESSION;\n"
'''.strip().format(version=version, date=date)

#
# Create the file for .properties strings
#
prop_patt = re.compile(r'^([a-zA-Z0-9]+)\s*=\s*(\S.*)$')
unicode_patt = re.compile(r'\\u([0-9a-fA-F]{4})')
def unicode_repl(match):
    return unichr(int(match.group(1), 16))

os.mkdir(build_path(dst_dir, 'gui'))

def load_lang(lang):
    key_list = []
    key_dict = {}
    suffix = '_' + lang + '.properties'
    for file in os.listdir(build_path(gui_dir, lang)):
        if file.endswith(suffix):
            file_base = file[:-len(suffix)]
            with open(os.path.join(gui_dir, lang, file), encoding='ISO-8859-1') as src:
                for line in src:
                    match = prop_patt.match(line)
                    if match:
                        key = file_base + ':' + match.group(1)
                        value = match.group(2).replace('"', '\\"')
                        value = unicode_patt.sub(unicode_repl, value)
                        key_list.append(key)
                        key_dict[key] = value
    return key_list, key_dict

en_keys, en_dict = load_lang('en')
with open(build_path(dst_dir, 'gui', 'gui.pot'), 'w', encoding='utf-8') as dst:
    dst.write(header + '\n')
    for key in en_keys:
        dst.write('\n')
        dst.write('msgctxt {ctxt}\n'.format(ctxt=key))
        dst.write('msgid "{msgid}"\n'.format(msgid=en_dict[key]))
        dst.write('msgstr "{xlate}"\n'.format(xlate=''))
        
for lang in known_langs:
    lang_keys, lang_dict = load_lang(lang)
    with open(build_path(dst_dir, 'gui', lang + '.po'), 'w', encoding='utf-8') as dst:
        dst.write(header + '\n')
        for key in en_keys:
            msgid = en_dict[key]
            if key in lang_dict:
                xlate = lang_dict[key]
            else:
                xlate = ''
            dst.write('\n')
            dst.write('msgctxt {ctxt}\n'.format(ctxt=key))
            dst.write('msgid "{msgid}"\n'.format(msgid=msgid))
            dst.write('msgstr "{xlate}"\n'.format(xlate=xlate))

tarball = tarfile.open(dst_tarball, 'w:gz')
tarball.add(dst_dir, '.')
tarball.close()

if not keep_temporary:
    shutil.rmtree(dst_dir)
    
print('POT creation completed')