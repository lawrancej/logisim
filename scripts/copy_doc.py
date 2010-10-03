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

def handle_img_tag(image_paths_xx, image_paths_en, cwd, xx_src, en_src):
    img_attr_re = re.compile('\s([a-zA-Z0-9-]+)="?([^"> \r\n\t]+)"?')
    def handle_tag(match):
        values = {}
        pieces = ['<img']
        for attr_match in img_attr_re.finditer(match.group()):
            attr = attr_match.group(1)
            values[attr] = attr_match.group(2)
            if attr not in ['src', 'width', 'height']:
                pieces.append(attr_match.group())
        if 'src' in values:
            src_val = values['src']
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
                src_key = (src_base, src_file)
                if src_key in image_paths_xx:
                    _dummy_, width, height = image_paths_xx[src_key]
                elif src_key in image_paths_en:
                    _dummy_, width, height = image_paths_en[src_key]
                    base_rel = os.path.relpath(xx_src, cwd)
                    src_val = base_rel + '/../en/' + src_base + '/' + src_file
            pieces.append('src="' + src_val.replace('\\', '/') + '"')
            if (src_val.endswith('.gif') and 'width' in values
                    and 'height' in values and values['width'] == '32'
                    and values['height'] == '32'):
                width, height = 32, 32 
            if width is not None:
                pieces.append('width="' + str(width) + '"')
            if height is not None:
                pieces.append('height="' + str(height) + '"')
        return ' '.join(pieces) + '>'
    return handle_tag

def do_copy(src_dir, dst_dir):
    if not os.path.exists(dst_dir):
        os.mkdir(dst_dir)
        
    en_src = build_path(src_dir, 'en')
    doc_locales = []
    for locale in os.listdir(src_dir):
        locale_src = build_path(src_dir, locale)
        locale_dst = build_path(dst_dir, locale)
        if len(locale) == 2 and os.path.isdir(locale_src):
            doc_locales.append((locale, locale_src, locale_dst))
    
    # Identify image files - first see which image files are unique to language
    image_paths = {}        
    for locale, locale_src, locale_dst in doc_locales:
        image_paths[locale] = _find_image_paths(locale_src, en_src)

    image_paths_en = image_paths['en']
    img_re = re.compile(r'<img[^>]*>')
        
    for locale, locale_src, locale_dst in doc_locales:
        os.mkdir(locale_dst)
        image_paths_xx = image_paths[locale]
        for base_src, dirs, files in os.walk(locale_src):
            for d in dirs[:]:
                if d.startswith('.'):
                    dirs.remove(d)

            base_rel = os.path.relpath(base_src, locale_src)
            base_dst = build_path(locale_dst, base_rel)
            img_handler = handle_img_tag(image_paths_xx, image_paths_en,
                                         base_src, locale_src, en_src)
            for dir in dirs:
                os.mkdir(build_path(base_dst, dir))
            for file in files:
                file_src = build_path(base_src, file)
                file_dst = build_path(base_dst, file)
                if file.endswith('.gif') or file.endswith('.png'):
                    if (base_rel, file) in image_paths_xx:
                        shutil.copy(file_src, file_dst)
                elif file.endswith('.html'):
                    with open(file_src, 'r', encoding='utf-8') as html_file:
                        html_text = html_file.read()
                    html_text = img_re.sub(img_handler, html_text)
                    with open(file_dst, 'w', encoding='utf-8') as html_file:
                        html_file.write(html_text)
                else:
                    shutil.copy(file_src, file_dst)

    for locale, locale_src, locale_dst in doc_locales:
        map_dst = build_path(locale_dst, 'map.jhm')
        if locale != 'en' and not os.path.exists(map_dst):
            map_src = build_path(en_src, 'map.jhm')
            shutil.copy(map_src, map_dst)
