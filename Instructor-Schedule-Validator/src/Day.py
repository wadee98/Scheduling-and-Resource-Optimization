class Day:
    def __init__(self,day,list):
        self.day = day
        self.items=list

    def __str__(self):
        temp = ""
        for item in self.items:
            temp += str(item)+" "
        return self.day+ " " + temp

    def __repr__(self):# this is to solve the problem when days are in a list so that we can use the str method
        return self.__str__()

    def __eq__(self, other):
        return True if self.day == other.day else False