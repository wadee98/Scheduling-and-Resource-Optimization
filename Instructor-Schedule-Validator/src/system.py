
from scheduleParser import Parser
from scheduleValidator import Validator
from policy.policyConfig import PolicyConfig as p
paths =["schedules.txt","codes.txt","instructorCodes.txt"]
print("Reading Files .......".title())
print("Loading Data ......",end = "\n\n")
data = Parser(paths[0],paths[1],paths[2])


for instructor in data.instructors:
    print(f"\nValidating Dr. {instructor.firstName.capitalize()} {instructor.lastName.capitalize()} schedule".title())
    validator = Validator(instructor)

    print("\n1. Validating Teaching Load ......".title())
    instructorLoad =validator.calculateTeachingLoad()
    if p.MIN_TEACHING_LOAD<=instructorLoad<=p.MAX_TEACHING_LOAD:
        print(f"Valid Teaching Load of {validator.calculateTeachingLoad()} Hours".title())
    else:
        if(instructorLoad<=p.MAX_TEACHING_LOAD):
            print(f"Invalid Teaching Load. It is {str(instructorLoad)} Which is Less Than 12 Hours".title())
        else:
            print(f"Invalid Teaching Load. It is {str(instructorLoad)} Which is More Than 18 Hours".title())

    print("\n2. Validating Office Hours Ratio ......".title())
    instructorOH = validator.calculateOfficeHours()
    if instructorOH>=p.OFFICE_HOURS_RATIO*instructorLoad:
        print("Valid Office Hours Ratio For that Specific Load".title())
    else:
        print(f"Invalid Office Hours Ratio: {int(instructorOH*100/instructorLoad)}%".title())

    print("\n3. Validating Teaching Days Count ......".title())
    if validator.calculateTeachingDays()>=p.MIN_TEACHING_DAYS:
        print("Valid Teaching Days Distribution".title())
    else:
        print("Invalid Teaching Days Distribution".title())

    print("\n4. Validating If Time Conflict Exists ......".title())
    print(f"{validator.detectTimeConflict()} Overlaps Detected!".title())

    print("\n5. Validating Consecutive Teaching Rule ......".title())
    if not validator.detectConsecutiveTeaching():print("No More Than Two Courses/Labs are scheduled consecutively without a break. ".title())

    print("\n6. Validating Allowed Teaching Days  ......".title())
    if validator.validateAllowedDays():print("courses are in valid days".title())

    print("\n7. Course and Lab Code Validation   ......".title())
    if validator.validateCodes(data.codes):print("all courses codes are valid".title())

    print("\n8. Instructor Preference Validation   ......".title())
    if validator.validatePreferences(data.instructorsCodes):print("all courses codes are preferred by this instructor".title())



