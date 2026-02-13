import os
import xml.etree.ElementTree as ET

def get_keys_from_file(file_path):
    keys = set()
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()
        for child in root:
            if 'name' in child.attrib:
                keys.add(child.attrib['name'])
    except Exception as e:
        pass # Ignore errors for now
    return keys

def main():
    res_dir = r"c:\Users\DELL\StudioProjects\ContactsJetpackcompose\app\src\main\res"
    base_strings_path = os.path.join(res_dir, "values", "strings.xml")
    
    if not os.path.exists(base_strings_path):
        print("Base strings.xml not found!")
        return

    base_keys = get_keys_from_file(base_strings_path)
    print(f"Base keys: {len(base_keys)}")

    incomplete_langs = []

    for item in os.listdir(res_dir):
        if item.startswith("values-") and item != "values":
            lang_dir = os.path.join(res_dir, item)
            if os.path.isdir(lang_dir):
                lang_strings_path = os.path.join(lang_dir, "strings.xml")
                if os.path.exists(lang_strings_path):
                    lang_keys = get_keys_from_file(lang_strings_path)
                    missing = base_keys - lang_keys
                    if missing:
                        print(f"{item}: Missing {len(missing)} keys")
                        incomplete_langs.append(item)
    
    if not incomplete_langs:
        print("All languages are complete!")

if __name__ == "__main__":
    main()
