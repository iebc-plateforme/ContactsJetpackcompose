import re

# Adlam character mapping (Latin to Adlam)
latin_to_adlam = {
    'a': '𞤢', 'A': '𞤀',
    'b': '𞤦', 'B': '𞤄',
    'ɓ': '𞤩', 'Ɓ': '𞤇',
    'c': '𞤷', 'C': '𞤕',
    'd': '𞤣', 'D': '𞤁',
    'ɗ': '𞤯', 'Ɗ': '𞤍',
    'e': '𞤫', 'E': '𞤉',
    'f': '𞤬', 'F': '𞤊',
    'g': '𞤺', 'G': '𞤘',
    'h': '𞤸', 'H': '𞤖',
    'i': '𞤭', 'I': '𞤋',
    'j': '𞤶', 'J': '𞤔',
    'k': '𞤳', 'K': '𞤑',
    'l': '𞤤', 'L': '𞤂',
    'm': '𞤥', 'M': '𞤃',
    'n': '𞤲', 'N': '𞤐',
    'ŋ': '𞤻', 'Ŋ': '𞤙',
    'ñ': '𞤻', 'Ñ': '𞤙',
    'o': '𞤮', 'O': '𞤌',
    'p': '𞤨', 'P': '𞤆',
    'r': '𞤪', 'R': '𞤈',
    's': '𞤧', 'S': '𞤅',
    't': '𞤼', 'T': '𞤚',
    'u': '𞤵', 'U': '𞤓',
    'w': '𞤱', 'W': '𞤏',
    'y': '𞤴', 'Y': '𞤒',
    'ƴ': '𞤴', 'Ƴ': '𞤒',
    'z': '𞥀', 'Z': '𞤞',
    # Digits
    '0': '𞥐', '1': '𞥑', '2': '𞥒', '3': '𞥓', '4': '𞥔',
    '5': '𞥕', '6': '𞥖', '7': '𞥗', '8': '𞥘', '9': '𞥙',
}

def convert_to_adlam(text):
    result = []
    i = 0
    while i < len(text):
        # Skip format specifiers like %s, %d, %1$d
        if text[i] == '%':
            j = i + 1
            while j < len(text) and (text[j].isdigit() or text[j] in 'sd$'):
                j += 1
            result.append(text[i:j])
            i = j
            continue
        # Skip escaped characters
        if text[i] == '\\' and i + 1 < len(text):
            result.append(text[i:i+2])
            i += 2
            continue
        # Convert character
        char = text[i]
        result.append(latin_to_adlam.get(char, char))
        i += 1
    return ''.join(result)

# Convert string values to Adlam
def convert_string_content(match):
    tag_start = match.group(1)
    content = match.group(2)
    tag_end = match.group(3)
    # Convert content to Adlam
    adlam_content = convert_to_adlam(content)
    return tag_start + adlam_content + tag_end

# Read the Latin Fulfulde file
with open('app/src/main/res/values-b+ff+Latn/strings.xml', 'r', encoding='utf-8') as f:
    content = f.read()

# Pattern to match string content
pattern = r'(<string[^>]*>)(.*?)(</string>)'
adlam_content = re.sub(pattern, convert_string_content, content, flags=re.DOTALL)

# Write the Fulfulde Adlam file
with open('app/src/main/res/values-b+ff+Adlm/strings.xml', 'w', encoding='utf-8') as f:
    f.write(adlam_content)

print('Fulfulde Adlam translation created successfully!')

# Also create Pulaar versions (same content for now)
# Write the Pulaar Latin file (copy of Fulfulde Latin)
with open('app/src/main/res/values-b+ff+Latn/strings.xml', 'r', encoding='utf-8') as f:
    content = f.read()

with open('app/src/main/res/values-b+fuf+Latn/strings.xml', 'w', encoding='utf-8') as f:
    f.write(content)

print('Pulaar Latin translation created successfully!')

# Write the Pulaar Adlam file (copy of Fulfulde Adlam)
with open('app/src/main/res/values-b+ff+Adlm/strings.xml', 'r', encoding='utf-8') as f:
    content = f.read()

with open('app/src/main/res/values-b+fuf+Adlm/strings.xml', 'w', encoding='utf-8') as f:
    f.write(content)

print('Pulaar Adlam translation created successfully!')
