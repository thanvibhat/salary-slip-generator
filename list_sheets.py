import openpyxl
wb = openpyxl.load_workbook('d:/college/OOPJ/salary-slip-generator-main/input/Salary Statement - 2025.xlsx', read_only=True)
print(wb.sheetnames)
