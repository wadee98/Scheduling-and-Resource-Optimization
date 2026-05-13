
class Instructor():
    def __init__(self,id,firstName,lastName):
        self.id = id
        self.firstName = firstName.lower()
        self.lastName = lastName.lower()
        self.scheduleDays = []


    def addDay(self, scheduleItem):
        self.scheduleDays.append(scheduleItem)

    def __str__(self):
        temp="\n"
        for day in self.scheduleDays:
            temp+=str(day)+"\n"
        return self.id + " " + self.firstName+ " " + self.lastName + " "+temp
