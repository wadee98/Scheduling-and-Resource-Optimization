
from instructor import Instructor
from scheduleItem import ScheduleItem
from Day import  Day
import re
class Parser:
    def __init__(self, schedules, codes, instructorCodes):
        self.instructors = []
        self.instructorsCodes={}
        self.codes = {"OH"}
        self.readInstrcotrsFiles(schedules)
        self.parseCodes(codes)
        self.parseInstructorCodes(instructorCodes)

    def readInstrcotrsFiles(self,path):
        try:
            f = open(path,'r')
            lines =f.readlines()
            if not lines:print("There are no schedules in the file".title())
            for line in lines:
                if not line.strip(): continue

                if re.match(r"^\d+",line):
                    try:
                        parts = line.split(',')
                        insId = parts[0].strip()
                        insFirstName = parts[1].strip()
                        insLastName = parts[2].strip()
                        self.instructors.append(Instructor(insId, insFirstName, insLastName))
                    except:
                        print(f"Invalid Instructor header: {line}")
                else:
                    if len(self.instructors) ==0:
                        print(f"Invalid line:{line.strip()}, there is no Instructor header before it".title())
                    else:
                        try:
                            dayItems=[]
                            parts = line.split('|')
                            day = parts[0].strip().upper()
                            items = parts[1].strip().split(';')
                            if not parts[1].strip():continue
                            for item in items:
                                if not item.strip():continue
                                startBracket = item.find("[")
                                endBracket = item.find("]")
                                timeRange = item[startBracket+1:endBracket]
                                corseCode = item[endBracket+1:].strip()
                                time = timeRange.split('-')
                                startTime = time[0].strip()
                                endTime = time[1].strip()

                                if ":" in startTime:
                                    parts = startTime.split(":")
                                    hours = int(parts[0].strip())
                                    minutes = int(parts[1].strip())
                                    if hours < 8 :
                                        hours +=12
                                    hours *= 60
                                    convertedStartTime = hours + minutes
                                else:
                                    hours = int(startTime)
                                    if hours < 8:
                                        hours += 12
                                    convertedStartTime = hours * 60

                                if ":" in endTime:
                                    parts = endTime.split(":")
                                    hours = int(parts[0].strip())
                                    minutes = int(parts[1].strip())
                                    if hours < 8 :
                                        hours +=12
                                    hours *= 60
                                    convertedEndTime = hours + minutes
                                else:
                                    hours = int(endTime)
                                    if hours < 8:
                                        hours += 12
                                    convertedEndTime = hours * 60
                                scheduleItem = ScheduleItem(convertedStartTime,convertedEndTime,corseCode)
                                dayItems.append(scheduleItem)
                            dayObj = Day(day,dayItems)
                            if not dayObj in self.instructors[-1].scheduleDays:
                                self.instructors[-1].addDay(dayObj)
                            else: print(f"There is a duplicated day or more: the only accepted day is the first one".title())
                        except:
                            print(f"Invalid Day: {line}")
        except FileNotFoundError:
            print("Schedules File not found")



    def parseInstructorCodes(self, path):
        try:
            file = open(path,"r")
            lines= file.readlines()
            if not lines: print("Preferences file is empty".title())
            for line in lines:
                if not line.strip(): continue
                parts= line.split(":")
                if len(parts) <2:
                    print(f"there are no preferences for the instructor with id {parts[0]}".title())
                    continue
                ls = parts[1].split(";")
                for i in range(len(ls)):
                    ls[i]= ls[i].strip()
                self.instructorsCodes[parts[0].strip()]=ls
        except FileNotFoundError:
            print("Instructor Codes File not found")


    def parseCodes(self,path):
        try:
            file = open(path,"r")
            lines= file.readlines()
            for line in lines:
                if line.strip():
                    searchObj=re.search(r"[A-Za-z]+\d+",line)
                    if(searchObj):
                        self.codes.add(searchObj.group().strip())

        except FileNotFoundError:
            print("Codes File not found")