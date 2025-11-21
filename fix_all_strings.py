import os
import re
import xml.etree.ElementTree as ET

def fix_strings_file(file_path):
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()
        modified = False

        for string in root.findall('string'):
            if string.text:
                original_text = string.text
                print(f"Original text: {original_text}")

                # Escape apostrophes, but only if they are not already escaped
                new_text = re.sub(r"(?<!\\)'", r"\\\'", original_text)

                # Fix non-positional format strings
                placeholders = re.findall(r'%[sd]', new_text)
                if len(placeholders) > 1:
                    count = 1
                    def replace_placeholder(match):
                        nonlocal count
                        replacement = f'%{count}${match.group(0)[-1]}'
                        count += 1
                        return replacement
                    new_text = re.sub(r'%[sd]', replace_placeholder, new_text)

                print(f"New text: {new_text}")

                if new_text != original_text:
                    string.text = new_text
                    modified = True
        
        if modified:
            tree.write(file_path, encoding='utf-8', xml_declaration=True)
            print(f"Fixed {file_path}")

    except ET.ParseError as e:
        print(f"Error parsing {file_path}: {e}")
    except Exception as e:
        print(f"Error processing {file_path}: {e}")


def fix_all_strings(res_path):
    for dir_name in os.listdir(res_path):
        if dir_name.startswith('values'):
            dir_path = os.path.join(res_path, dir_name)
            strings_path = os.path.join(dir_path, 'strings.xml')
            if os.path.isfile(strings_path):
                fix_strings_file(strings_path)

if __name__ == '__main__':
    fix_all_strings('app/src/main/res')
    print("Finished fixing all strings.xml files.")