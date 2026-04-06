import os, glob, re

# Find all Java files in src
paths = glob.glob('src/main/java/**/*.java', recursive=True)
count = 0

# Match @RequestParam or @PathVariable that are NOT already parameterized (don't have an opening parenthesis right after)
# Pattern: Annotation followed by spaces, then a type (capitalized word, potentially with generics), then the parameter name.
# Preceding with (?![\(]) to ensure we don't match already fixed annotations.

param_regex = re.compile(r'@(RequestParam|PathVariable)\s*(?!\()([A-Z][a-zA-Z0-9_<>]*)\s+([a-zA-Z0-9_]+)')

for p in paths:
    with open(p, 'r', encoding='utf-8') as f:
        content = f.read()

    new_content = param_regex.sub(r'@\1("\3") \2 \3', content)

    if new_content != content:
        with open(p, 'w', encoding='utf-8') as f:
            f.write(new_content)
        count += 1
        print(f"Updated: {p}")

print(f"Successfully processed {count} files.")
