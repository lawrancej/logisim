import filecmp
import os
import re
import shutil
from logisim_script import build_path

def get_doc_locales(src_dir):
    doc_locales = []
    for locale in os.listdir(src_dir):
        locale_src = build_path(src_dir, locale)
        contents_src = build_path(locale_src, 'html/contents.html')
        if (len(locale) == 2 and os.path.isdir(locale_src)
                and os.path.exists(contents_src)):
            doc_locales.append(locale)
    return doc_locales
            
def _get_png_dimension(png_path):
    with open(png_path, 'rb') as png_file:
        header = png_file.read(64)
    for i in range(2):
        start = 16 + i * 4
        dim = 0
        for j in range(4):
            dim = 256 * dim + (header[start + j] & 0xFF)
        if i == 0:
            width = dim
        else:
            height = dim
    return (width, height)

def _find_image_paths(locale_src, en_src):
    image_paths = {}
    for base_src, dirs, files in os.walk(locale_src):
        base_rel = os.path.relpath(base_src, locale_src)
        for d in dirs[:]:
            if d.startswith('.'):
                dirs.remove(d)
        for file in files:
            if file.endswith('.gif') or file.endswith('.png'):
                img_xx = build_path(base_src, file)
                if locale_src == en_src:
                    same = False
                else:
                    img_en = build_path(en_src, base_rel, file)
                    if os.path.exists(img_xx):
                        same = os.path.exists(img_en) and filecmp.cmp(img_en, img_xx, False)
                    else:
                        same = False
                if not same:
                    if file.endswith('.gif'):
                        width, height = (16, 16)
                    elif file.endswith('.png'):
                        width, height = _get_png_dimension(img_xx)
                    image_paths[(base_rel, file)] = (img_xx, width, height)
    return image_paths

def _handle_tag(image_paths_xx, image_paths_en, cwd, xx_src, en_src):
    img_attr_re = re.compile('\s([a-zA-Z0-9-]+)="?([^"> \r\n\t]+)"?', re.MULTILINE | re.DOTALL)
    def handle_tag(match):
        tag_name = 'img'
        pieces = ['<' + tag_name]
        handled_attrs = ['src', 'width', 'height']
        src_attr = 'src'
        tag_end = '>'
        
        values = {}
        for attr_match in img_attr_re.finditer(match.group()):
            attr = attr_match.group(1)
            values[attr] = attr_match.group(2)
            if attr not in handled_attrs:
                pieces.append(attr_match.group())
        if src_attr in values:
            src_val = values[src_attr]
            src_path = os.path.normpath(build_path(cwd, src_val))
            width = None
            height = None
            if src_path.startswith(en_src):
                src_rel = os.path.relpath(src_path, en_src)
                src_base, src_file = os.path.split(src_rel)
                src_key = (src_base, src_file)
                if src_key in image_paths_en:
                    _dummy_, width, height = image_paths_en[src_key]
            elif src_path.startswith(xx_src):
                src_rel = os.path.relpath(src_path, xx_src)
                src_base, src_file = os.path.split(src_rel)
                src_base = src_base.replace('\\', '/')
                src_key = (src_base, src_file)
                if src_key in image_paths_xx:
                    _dummy_, width, height = image_paths_xx[src_key]
                elif src_key in image_paths_en:
                    _dummy_, width, height = image_paths_en[src_key]
                    base_rel = os.path.relpath(xx_src, cwd).replace('\\', '/')
                    src_path = build_path(en_src, src_base, src_file)
            src_new = os.path.relpath(src_path, cwd).replace('\\', '/')
            pieces.append(src_attr + '="' + src_new + '"')
            if (src_val.endswith('.gif') and 'width' in values
                    and 'height' in values and values['width'] == '32'
                    and values['height'] == '32'):
                width, height = 32, 32 
            if 'width' in handled_attrs and width is not None:
                pieces.append('width="' + str(width) + '"')
            if 'height' in handled_attrs and height is not None:
                pieces.append('height="' + str(height) + '"')
        return ' '.join(pieces) + tag_end
    return handle_tag

def copy_files(locale, src_dir, dst_dir, translate_html=None,
               confirm_replace=None):
    en_src = build_path(src_dir, 'en')
    locale_src = build_path(src_dir, locale)
    locale_dst = build_path(dst_dir, locale)
    if not os.path.exists(locale_dst):
        os.mkdir(locale_dst)

    image_paths_en = _find_image_paths(en_src, en_src)
    image_paths_locale = _find_image_paths(locale_src, en_src)    
    img_re = re.compile(r'<img\s[^>]*>', re.MULTILINE | re.DOTALL)
    
    for base_src, dirs, files in os.walk(locale_src):
        base_rel = os.path.relpath(base_src, locale_src)
        base_dst = build_path(locale_dst, base_rel)
        img_handler = _handle_tag(image_paths_locale, image_paths_en,
                                     base_src, locale_src, en_src)

        for dir in dirs[:]:
            if dir.startswith('.'):
                dirs.remove(dir)
            else:
                dir_dst = build_path(base_dst, dir)
                if not os.path.exists(dir_dst):
                    os.mkdir(build_path(base_dst, dir))
        for file in files:
            file_src = build_path(base_src, file)
            file_dst = build_path(base_dst, file)
            file_rel = build_path(base_rel, file)
            file_dst_exists = os.path.exists(file_dst)
            if (file_dst_exists and confirm_replace is not None
                    and confirm_replace(file_rel)):
                os.remove(file_dst)
                file_dst_exists = False
            if not file_dst_exists:
                if file.endswith('.gif') or file.endswith('.png'):
                    if (base_rel, file) in image_paths_locale:
                        shutil.copy(file_src, file_dst)
                elif file.endswith('.html') or file.endswith('.jhm'):
                    try:
                        with open(file_src, 'r', encoding='utf-8') as html_file:
                            html_text = html_file.read()
                        if html_text.strip() == '':
                        	file_src_en = build_path(en_src, base_rel, file)
                        	with open(file_src_en, 'r', encoding='utf-8') as html_en_file:
                        		html_text = html_en_file.read() 
                        html_text = img_re.sub(img_handler, html_text)
                        if translate_html is not None:
                            file_rel = file_rel.replace('\\', '/')
                            html_text = translate_html(html_text, file_rel)
                        with open(file_dst, 'w', encoding='utf-8') as html_file:
                            html_file.write(html_text)
                    except UnicodeDecodeError as e:
                        print('error copying ' + file_rel + ': ' + str(e))
                else:
                    shutil.copy(file_src, file_dst)

def copy_files_all_locales(src_dir, dst_dir):
    if not os.path.exists(dst_dir):
        os.mkdir(dst_dir)
    for locale in get_doc_locales(src_dir):
        copy_files(locale, src_dir, dst_dir)
                    
def get_contents(locale, src_dir):
    re_contents = re.compile(r'<a [^>]*id="([^"]*)">([^<]*)</a>')
    locales = [locale]
    if locale != 'en':
        locales.append('en')
    for loc in locales:
        contents_src = build_path(src_dir, loc, 'html/contents.html')
        ids = {}
        if os.path.exists(contents_src):
            with open(contents_src, encoding='utf-8') as contents_file:
                contents_text = contents_file.read()
            for contents_id in re_contents.finditer(contents_text):
                id = contents_id.group(1)
                text = contents_id.group(2)
                text = text.replace('"', '&quot;')
                ids[id] = text
        if loc == locale:
            ids_locale = ids
        if loc == 'en':
            ids_en = ids 
        
    re_tocitem = re.compile('(<tocitem[^>]*target=")([^"]*)(")([^>]*>)')

    def tocitem_repl(match):
        target_pre = match.group(1)
        target = match.group(2)
        target_post = match.group(3)
        tocitem_end = match.group(4)
        
        if target in ids_locale:
            text = ' text="' + ids_locale[target] + '"'
        elif target in ids_en:
            text = ' text="' + ids_en[target] + '"'
        else:
            text = target
        
        return target_pre + target + target_post + text + tocitem_end
                
    base_path = build_path(src_dir, 'support/base-contents.xml')
    with open(base_path, encoding='utf-8') as base_file:
        base_text = base_file.read()
    return re_tocitem.sub(tocitem_repl, base_text)
                    
def get_map(locale, src_dir):
    locales = [locale]
    if locale != 'en':
        locales.append('en')
    for loc in locales:
        locale_src = build_path(src_dir, loc)
        urls = {}
        for path, dirs, files in os.walk(locale_src):
            if '.svn' in dirs:
                dirs.remove('.svn')
            for file in files:
                rel_path = os.path.relpath(build_path(path, file), locale_src)
                urls[rel_path.replace('\\', '/')] = True
        if loc == locale:
            urls_locale = urls
        if loc == 'en':
            urls_en = urls
    
    re_mapid = re.compile('(<mapID[^>]*url=")([^"]*)("[^>]*>)')
    def mapid_repl(match):
        url_pre = match.group(1)
        url = match.group(2)
        url_post = match.group(3)
        
        if url in urls_locale:
            url = locale + '/' + url
        elif url in urls_en:
            url = 'en/' + url
        
        return url_pre + url + url_post
            
    base_map = build_path(src_dir, 'support/base-map.jhm')
    with open(base_map, encoding='utf-8') as base_file:
        base_text = base_file.read();
    return re_mapid.sub(mapid_repl, base_text)

def build_contents(src_dir, dst_dir):
    for locale in get_doc_locales(src_dir):
        contents_dst = build_path(dst_dir, locale, 'contents.xml')
        if not os.path.exists(contents_dst):
            contents_text = get_contents(locale, src_dir)
            with open(contents_dst, 'w', encoding='utf-8') as contents_file:
                contents_file.write(contents_text)

def build_map(src_dir, dst_dir):
    for locale in get_doc_locales(src_dir):
        map_dst = build_path(dst_dir, 'map_' + locale + '.jhm')
        if not os.path.exists(map_dst):
            map_text = get_map(locale, src_dir)
            with open(map_dst, 'w', encoding='utf-8') as map_file:
                map_file.write(map_text)

def build_helpset(src_dir, dst_dir):
    base_helpset = build_path(src_dir, 'support/base-doc.hs')
    with open(base_helpset, encoding='utf-8') as base_file:
        base_text = base_file.read();
    
    re_lang = re.compile('{lang}')
    for locale in get_doc_locales(src_dir):
        helpset_dst = build_path(dst_dir, 'doc_' + locale + '.hs')
        if not os.path.exists(helpset_dst):
            helpset_text = re_lang.sub(locale, base_text)
            with open(helpset_dst, 'w', encoding='utf-8') as helpset_file:
                helpset_file.write(helpset_text)
                