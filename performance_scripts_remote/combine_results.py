import os
import json
import csv

# Directory where the JSON files are located
json_directory = "./"

# Output CSV file name
output_csv = "combined_data.csv"

# Function to check if a file is a valid JSON file and starts with "memory"
def is_valid_json_file(filename):
    return filename.startswith("memory")

# List to store all JSON data
all_data = []

# Get a list of JSON files in the directory
json_files = [f for f in os.listdir(json_directory) if is_valid_json_file(f)]

# Process each JSON file and extract the required data
for json_file in json_files:
    with open(os.path.join(json_directory, json_file), "r") as file:
        data = json.load(file)
        all_data.append(data)

all_data.sort(key=lambda x: x["EXP file name"])

# Write the combined data to the output CSV file
with open(output_csv, "w", newline="") as csv_file:
    csv_writer = csv.DictWriter(csv_file, fieldnames=all_data[0].keys())
    csv_writer.writeheader()
    csv_writer.writerows(all_data)

print(f"Data from {len(all_data)} JSON files combined and written to '{output_csv}'.")
