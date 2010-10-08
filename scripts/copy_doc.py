import filecmp
import os
import re
import shutil
from logisim_script import build_path
            
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
                    same = filecmp.cmp(img_en, img_xx, False)
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
                    src_val = base_rel + '/../en/' + src_base + '/' + src_file
            pieces.append(src_attr + '="' + src_val.replace('\\', '/') + '"')
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

def _find_locales(src_dir, dst_dir):
    doc_locales = []
    for locale in os.listdir(src_dir):
        locale_src = build_path(src_dir, locale)
        locale_dst = build_path(dst_dir, locale)
        if len(locale) == 2 and os.path.isdir(locale_src):
            doc_locales.append((locale, locale_src, locale_dst))
    return doc_locales

def do_copy(src_dir, dst_dir):
    if not os.path.exists(dst_dir):
        os.mkdir(dst_dir)
        
    en_src = build_path(src_dir, 'en')
    doc_locales = _find_locales(src_dir, dst_dir)
    
    # Identify image files - first see which image files are unique to language
    image_paths = {}        
    for locale, locale_src, locale_dst in doc_locales:
        image_paths[locale] = _find_image_paths(locale_src, en_src)

    image_paths_en = image_paths['en']
    img_re = re.compile(r'<img\s[^>]*>', re.MULTILINE | re.DOTALL)
        
    for locale, locale_src, locale_dst in doc_locales:
        os.mkdir(locale_dst)
        image_paths_xx = image_paths[locale]
        for base_src, dirs, files in os.walk(locale_src):
            for d in dirs[:]:
                if d.startswith('.'):
                    dirs.remove(d)

            base_rel = os.path.relpath(base_src, locale_src)
            base_dst = build_path(locale_dst, base_rel)
            img_handler = _handle_tag(image_paths_xx, image_paths_en,
                                         base_src, locale_src, en_src)
            for dir in dirs:
                os.mkdir(build_path(base_dst, dir))
            for file in files:
                file_src = build_path(base_src, file)
                file_dst = build_path(base_dst, file)
                if file.endswith('.gif') or file.endswith('.png'):
                    if (base_rel, file) in image_paths_xx:
                        shutil.copy(file_src, file_dst)
                elif file.endswith('.html') or file.endswith('.jhm'):
                    with open(file_src, 'r', encoding='utf-8') as html_file:
                        html_text = html_file.read()
                    html_text = img_re.sub(img_handler, html_text)
                    with open(file_dst, 'w', encoding='utf-8') as html_file:
                        html_file.write(html_text)
                else:
                    shutil.copy(file_src, file_dst)

def build_contents(src_dir, dst_dir):
    doc_locales = _find_locales(src_dir, dst_dir)
    
    xn_ids = {}
    re_contents = re.compile('<a [^>]*id="([^"]*)">([^<]*)</a>')
    for locale, locale_src, locale_dst in doc_locales:
        contents_src = build_path(locale_src, 'html/contents.html')
        if os.path.exists(contents_src):
            with open(contents_src, encoding='utf-8') as contents_file:
                contents_text = contents_file.read()
            ids = {}
            for contents_id in re_contents.finditer(contents_text):
                id = contents_id.group(1)
                text = contents_id.group(2)
                text = text.replace('"', '&quot;')
                ids[id] = text
            xn_ids[locale] = ids
            
    base_path = build_path(src_dir, 'circs/base-contents.xml')
    with open(base_path, encoding='utf-8') as base_file:
        base_text = base_file.read();
    
    re_tocitem = re.compile('(<tocitem[^>]*target=")([^"]*)(")([^>]*>)')
    for locale, locale_src, locale_dst in doc_locales:
        contents_dst = build_path(locale_dst, 'contents.xml')
        if not os.path.exists(contents_dst):
            def tocitem_repl(match):
                target_pre = match.group(1)
                target = match.group(2)
                target_post = match.group(3)
                tocitem_end = match.group(4)
                
                if locale in xn_ids and target in xn_ids[locale]:
                    text = ' text="' + xn_ids[locale][target] + '"'
                elif target in xn_ids['en']:
                    text = ' text="' + xn_ids['en'][target] + '"'
                else:
                    text = ''
                
                return target_pre + target + target_post + text + tocitem_end
            
            contents_text = re_tocitem.sub(tocitem_repl, base_text)
            with open(contents_dst, 'w', encoding='utf-8') as contents_file:
                contents_file.write(contents_text)

def build_map(src_dir, dst_dir):
    doc_locales = _find_locales(src_dir, dst_dir)
    
    xn_urls = {}
    for locale, locale_src, locale_dst in doc_locales:
        urls = {}
        for path, dirs, files in os.walk(locale_src):
            if '.svn' in dirs:
                dirs.remove('.svn')
            for file in files:
                rel_path = os.path.relpath(build_path(path, file), locale_src)
                urls[rel_path.replace('\\', '/')] = True
        xn_urls[locale] = urls
            
    base_map = build_path(src_dir, 'circs/base-map.jhm')
    with open(base_map, encoding='utf-8') as base_file:
        base_text = base_file.read();
    
    re_mapid = re.compile('(<mapID[^>]*url=")([^"]*)("[^>]*>)')
    for locale, locale_src, locale_dst in doc_locales:
        map_dst = build_path(dst_dir, 'map_' + locale + '.jhm')
        if not os.path.exists(map_dst):
            def mapid_repl(match):
                url_pre = match.group(1)
                url = match.group(2)
                url_post = match.group(3)
                
                if locale in xn_urls and url in xn_urls[locale]:
                    url = locale + '/' + url
                elif url in xn_urls['en']:
                    url = 'en/' + url
                
                return url_pre + url + url_post
            
            map_text = re_mapid.sub(mapid_repl, base_text)
            with open(map_dst, 'w', encoding='utf-8') as map_file:
                map_file.write(map_text)

def build_helpset(src_dir, dst_dir):
    doc_locales = _find_locales(src_dir, dst_dir)
            
    base_helpset = build_path(src_dir, 'circs/base-doc.hs')
    with open(base_helpset, encoding='utf-8') as base_file:
        base_text = base_file.read();
    
    re_lang = re.compile('{lang}')
    for locale, locale_src, locale_dst in doc_locales:
        helpset_dst = build_path(dst_dir, 'doc_' + locale + '.hs')
        if not os.path.exists(helpset_dst):
            helpset_text = re_lang.sub(locale, base_text)
            with open(helpset_dst, 'w', encoding='utf-8') as helpset_file:
                helpset_file.write(helpset_text)
