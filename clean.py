import pathlib
p = pathlib.Path('src/main/resources/salary_slip.jrxml')
content = p.read_text(encoding='utf-8')

# Remove exactly what user requested
content = content.replace(' positionType="Float"', '')
content = content.replace(' isRemoveLineWhenBlank="true"', '')
content = content.replace(' isBlankWhenNull="true"', '')

p.write_text(content, encoding='utf-8')
print("Done stripping attributes")
