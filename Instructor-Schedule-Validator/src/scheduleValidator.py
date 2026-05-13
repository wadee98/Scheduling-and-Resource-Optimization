from policy.policyConfig import PolicyConfig
class Validator:
    def __init__(self,instructor):
        self.instructor = instructor

    def calculateTeachingLoad(self):
        load = 0
        for day in self.instructor.scheduleDays:
            for scheduleItem in day.items:
                if scheduleItem.courseCode.lower() !="oh":
                    load += (scheduleItem.endTime - scheduleItem.startTime)/60
        return load

    def calculateOfficeHours(self):
        officeHours = 0
        for day in  self.instructor.scheduleDays:
            for scheduleItem in day.items:
                if scheduleItem.courseCode.lower() =="oh":
                    officeHours += (scheduleItem.endTime - scheduleItem.startTime)/60
        return officeHours

    def calculateTeachingDays(self):

        teachingDays = 0
        for day in self.instructor.scheduleDays:
            if day.day in PolicyConfig.MERGED_ALLOWED:
                teachingDays+=1
        return teachingDays

    def detectTimeConflict(self):
        overLaps = 0
        for day in self.instructor.scheduleDays:
            items= day.items
            length = len(items)

            for i in range(length-1):
                if items[i].endTime>items[i+1].startTime:
                    newStr = "Saturday" if day.day == "S" else "Monday" if day.day=="M" else "Tuesday" if day.day == "T" else "Wednesday" if day.day=="W" else "Thursday" if day.day =="TH" else day.day
                    print(f"Found An Overlap Between {str(items[i]).strip()} And {str(items[i+1]).strip()} in Day Symbol {newStr}".strip())
                    overLaps+=1
        return overLaps

    def detectConsecutiveTeaching(self):
        for day in self.instructor.scheduleDays:
            items= day.items
            count=0
            for item in items:
                if item.courseCode!="OH":
                    count+=1
                    if count>PolicyConfig.MAX_CONSECUTIVE:
                        newStr = "Saturday" if day.day == "S" else "Monday" if day.day=="M" else "Tuesday" if day.day == "T" else "Wednesday" if day.day=="W" else "Thursday" if day.day =="TH" else day.day
                        print(f"More than 2 consecutive courses are detected in {newStr}")
                        return True
                else: count=0
        return False
    def validateAllowedDays(self):
        smwGroup =set({})
        trGroup =set({})
        for day in self.instructor.scheduleDays:
            cd = day.day
            if not cd in PolicyConfig.MERGED_ALLOWED:
                print("Invalid: there are days other than the required groups".title())
                return False
            if cd in PolicyConfig.ALLOWED_DAYS[0]:
                for item in day.items:
                    smwGroup.add(item.courseCode)
            if cd in PolicyConfig.ALLOWED_DAYS[1]:
                for item in day.items:
                    trGroup.add(item.courseCode)

        intersection = smwGroup.intersection(trGroup)
        if("OH" in intersection): intersection.remove("OH")
        if intersection:
            print(f"Invalid; Involved Courses: {" ".join(intersection)}".title())
            return  False
        return True

    def validateCodes(self,codesSet):
        notValidCodes= set()
        for day in self.instructor.scheduleDays:
            for item in day.items:
                if(item.courseCode not in codesSet):
                    notValidCodes.add(item.courseCode)

        if notValidCodes:
            print("The following codes are not valid \n".title()+"\n".join(notValidCodes))
            return False

        return True
    def validatePreferences(self,preferencesDict):
        if self.instructor.id not in preferencesDict:
            print("the instructor has no preferences in the file preferences".title())
            return  False
        notValidCodes= set()

        for day in self.instructor.scheduleDays:
            for item in day.items:
                if(item.courseCode not in preferencesDict[self.instructor.id]):
                    notValidCodes.add(item.courseCode)

        if "OH" in notValidCodes: notValidCodes.remove("OH")

        if notValidCodes:
            print("The following codes are not preferred by this instructor \n".title()+"\n".join(notValidCodes))
            return False

        return True

