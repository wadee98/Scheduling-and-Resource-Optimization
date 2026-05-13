class ScheduleItem:
    def __init__(self,startTime,endTime,corseCode):
        self.startTime = startTime
        self.endTime = endTime
        self.courseCode = corseCode.upper()


    def __str__(self):
        hourStart = self.startTime // 60
        minuteStart = self.startTime % 60
        hourEnd = self.endTime // 60
        minuteEnd = self.endTime % 60

        hourStart = str(hourStart).zfill(2)
        minuteStart = str(minuteStart).zfill(2)
        hourEnd = str(hourEnd).zfill(2)
        minuteEnd = str(minuteEnd).zfill(2)

        return f"[{hourStart}:{minuteStart} - {hourEnd}:{minuteEnd}] {self.courseCode}"

    def __eq__(self, other):
        return self.courseCode==other.courseCode and self.startTime == other.startTime and self.endTime == other.endTime

    def __repr__(self):# this is when printing in lists
        return  self.__str__()