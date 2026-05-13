#!/bin/sh

file=""
echo "Insert the name of inputs file:"
read file
while [ ! -e "$file" ]
do
	echo "Error please insert a valid file"
	read file
done

s=
m=
t=
w=
th=
i=1
notDayChecker=0
while [ "$i" -le 5 ] ; do
	line=$(sed -n "${i}p" $file)

#here we tell the sed to not print everything, then print the specific line at i
#i represents the counter that will start from 1 to end of lines
#it will count to 5 ,do 5ith line then reach 6 and end

	day=$( echo $line | cut -d'|' -f1 )

#get the first field by by the delemiter "|" which represents the specific day

	workOfDay=$(echo $line | cut -d'|' -f2 | tr -d ' ')

#get the second field by the delemiter "|" which represents the work in this specific day

	if [ "$day" = 'S' ] ; then
		s=$workOfDay
	elif [  "$day" = 'M' ] ; then
		m=$workOfDay
	elif [  "$day" = 'T' ] ; then
		t=$workOfDay
	elif [  "$day" = 'W' ] ; then
		w=$workOfDay
	elif [  "$day" = 'Th' ] ; then
		th=$workOfDay
	fi
	i=$((i+1)) #increase counter

#simple conditions to determine which variable should be mapped to which day
#this is done in case the schedule isn't in order

done

for var in "$s" "$m" "$w" "$t" "$th"
do
	if [ -z "$var" ] ; then
		notDayChecker=$((notDayChecker+1))
	fi
done

#this loop will move around the variables one by one. with each one found empty (it isn't assigned so didn't exist in the input file),
#it will increase a counter called notDayChecker. if that counter is found to be 2 or more then the schedual isn't spanning along 4 days but less

all="$s;$m;$t;$w;$th"
all=$(printf "%s" "$all" | tr ';' '\n')

#we did the format like this to manipulate data easily in some parts of this project(standardizing the seperator as "new line" between each entery

teachLoad=0
ohLoad=0
# in minutes
for var in $all ; do
	timeSlot=$(echo "$var" | tr -d '[' | tr ']-–-' ',' | tr -s ',' ',' )

#every var represents a (course and a time ) as [number,number]String. we want to transform them as number,number,string
#at first tr, we removed the "[", then second tr will replace ] and different shapes of hyphen (based on the used format) by the ","
#the last one will ensure removing any extra comma if existed for some reason it isn't always important, but a protection if some weird syntax is found

	time1=$(echo "$timeSlot" | cut -d',' -f1) # get first feild which is course start time
	time2=$(echo "$timeSlot" | cut -d',' -f2) # course end time
	del=$(echo "$timeSlot" | cut -d',' -f3)	# last field represents the course name
	h1=$(echo $time1 | cut -d':' -f1) # if we had minutes it will return only hour part
	h2=$(echo $time2 | cut -d':' -f1)

	if echo $time1 | grep ':' > /dev/null ; then
		m1=$(echo $time1 | cut -d':' -f2)
	else
		m1=0
	fi

	if echo $time2 | grep ':' > /dev/null ; then
		m2=$(echo $time2 | cut -d':' -f2)
	else
		m2=0
	fi

#if either times have minutes part then add that to minutes variables for each time

	if [ "$h2" -lt "$h1" ] ; then
		h2=$((h2+12))
	fi

#if end time was bigger then it surely =(time+12) because it is 12 hour format

	if [ "$del" = "OH" ] ; then
		ohLoad=$(( ohLoad + (h2 * 60 + m2) - (h1 * 60 + m1) ))
	else
		teachLoad=$(( teachLoad + (h2 * 60 + m2) - (h1 * 60 + m1) ))
	fi

#each time we add to either loads the time by minutes

done

echo "Task 1: Total Teaching Load: $((teachLoad/60)) hours"
echo "===================================================="
echo "Task 2: Total Office Hours: $((ohLoad/60)) hours"
echo "===================================================="
echo "Task 3: Validating Teaching Load Range........."
if [ "$teachLoad" -lt 720 ] ; then
	printf "Invalid Schedule due to very low teaching load of about $((teachLoad/60)) hour\n\n"
elif [ "$teachLoad" -gt 1080 ] ; then
	printf "Invalid Schedule due to very high teaching load of about $((teachLoad/60)) hour\n\n"
else
	printf "Valid Schedule: Teaching load is within the allowed range (12-18 hours)\n\n"
fi

#720 minutes are 12 hours, 1080 minutes are 18 hours

echo "===================================================="

echo "Task 4: Validating Load-to-Office Hours Ratio........."
#we cannot use division it isn't precise we can use another way using substraction
# if we substracted 2 OH loads and we had a non negative number then the OH is less than 50% of teach
#load
temp=$((teachLoad - ( 2 * ohLoad )))
if [ "$temp" -gt 0 ] ; then
	echo "Invalid Schedule due to High Load-to-Office Hours Ratio"
	echo "The schedule should have more office hours or less teaching load"
else
	echo "Valid Schedule: Office hours meet the 50% (or more) requirement"
fi
echo "===================================================="

echo "Task 5: Validating Distribution Across Teaching Days........"
if [ "$notDayChecker" -gt 1 ] ; then
	echo "Invalid Schedule due to compressed schedule"
	echo "There are only $((5-notDayChecker)) teaching days"
else
	echo "Valid Schedule: Classes are distributed across $((5-notDayChecker)) teaching days"
fi
echo "===================================================="

# if "notDayChecker" counter is found to be 2 or more then the schedual isn't spanning along 4 days but less

echo "Task 6: Checking for Time conflicts........"
overLaps=0
isConOver=0

#this loop will solve task 6 and 7

for day in $s $m $t $w $th
do
	cons=0 #number of happening to found consecutive courses in a day
	dayTimes=""  # this will represent an array of times of courses in which each two repeated numbers represent the start and end for each course
	withoutOh="" # same as dayTimes but without OH times (so we can find consecutiveness by it )
	day=$(echo "$day" |tr ";" "\n") # to standardize the code so we can get use of previouse code
	for var in $day
	do
		timeSlot=$(echo "$var" | tr -d '[' | tr ']-–-' ',' | tr -s ',' ',' )
		time1=$(echo "$timeSlot" | cut -d',' -f1)
		time2=$(echo "$timeSlot" | cut -d',' -f2)
		del=$(echo "$timeSlot" | cut -d',' -f3)
		h1=$(echo $time1 | cut -d':' -f1)
		h2=$(echo $time2 | cut -d':' -f1)	
		if echo $time1 | grep ':' > /dev/null ; then
			m1=$(echo $time1 | cut -d':' -f2)
		else
			m1=0
		fi
		
		if echo $time2 | grep ':' > /dev/null ; then
			m2=$(echo $time2 | cut -d':' -f2)
		else
			m2=0
		fi
		
#all of latter was explained since it is exact code as in previouse part of this project

		if [ "$h2" -le 5 -a "$h2" -ge 1 ] ; then 
			h2=$((h2+12))
		fi
		if [ "$h1" -le 5 -a "$h1" -ge 1 ] ; then 
			h1=$((h1+12))
		fi

#here we don't compare between h1 and h2 like before, here we are assuming that those data can be incorrect,
#so if it happens to be one of them in range (1-5) we add 12; the university is closed at (1-5) AM so by conventions if 1-5 is found it is actually (13-17)

		h1=$((h1*60+m1))
		h2=$((h2*60+m2))

#after this assignment h1,h2 will represent the total time instead in minutes

		if [ ! "$del" = "OH" ] ; then
		withoutOh="$withoutOh$h1 $h2 "
		fi
		dayTimes="$dayTimes$h1 $h2 "


	done

	#here we should check for over lap for each dayTimes (it is like an array of times in which each
	#two consective numbers represents one slot, so we should check for each consecutive courses
	#the end time of first course and start time of second course if they overlap

	t1=2
	t2=3

	#to acheive that we should move on them one by one

	while [ 1 -eq 1 ]
	do
	t1T=$(echo $dayTimes | cut -d' ' -f$t1)
	t2T=$(echo $dayTimes | cut -d' ' -f$t2)
	
# this will start comparing between end of previouse course with start of next course.

	if [ -z "$t2T" ] ; then 
		break
	fi

# if at any point, the third field was empty then there is no need to continue as there no overlap with empty 
	
	if [ "$t2T" -lt "$t1T" ]
	then
		overLaps=$((overLaps+1)) #this will count how much overlaps there is
	fi
	
	t1=$(($t1+2))
	t2=$(($t2+2)) #increase counters

	done

	t1=2
	t2=3
	
#same as previouse loop but it will iterate through the withoutOh array instead of dayTimes 

	while [ 1 -eq 1 ]
	do
	t1T=$(echo $withoutOh | cut -d' ' -f$t1)
	t2T=$(echo $withoutOh | cut -d' ' -f$t2)
	
	if [ -z "$t2T" ] ; then 
		break
	fi

	
	if [ "$t2T" -eq "$t1T" ]
	then
		cons=$((cons+1))
	fi
	
# if two courses were consecutives, then end time of first course = start time of last course

	t1=$(($t1+2))
	t2=$(($t2+2))
	
	done

#at each day if cons variable found to be more than 1 then we have found more than allowed consecutives so make a flag to know 

	if [ "$cons" -gt 1 ]; then
		isConOver=1
	fi
	

done

echo "we found $overLaps overlaps"
echo "===================================================="

echo "Task 7: Validating Consecutive Teaching Rule......." 

if [ "$isConOver" -eq 1 ] ; then 
	echo "There are more than two courses assigned in consecutive time slots"
else
	echo "There are no more than two courses assigned in consecutive time slots"
fi
echo "===================================================="

echo "Task 8: Validating Allowed Teaching Days........."

invalidDayFound=0

#this loop will iterate through all lines in input file 

i=1
while [ 1 -eq 1  ] ; do
    line=$(sed -n "${i}p" $file)
    if [ -z "$line" ]; then
    break
    fi
    day=$( echo $line | cut -d'|' -f1 )

# if any line contained a day not supported like friday or sunday then invalid

    if [ ! "$day" = "S" ] && [ ! "$day" = "M" ] && [ ! "$day" = "T" ] && [ ! "$day" = "W" ] && [ ! "$day" = "Th" ] ; then
        invalidDayFound=1
    fi

    i=$((i+1))
done

# the course can actually be in different groups of days in this project and this is the logical thing; since one instructor can teach one course for more than one section
# suppose he give two sections. each section is in different group

if [ "$invalidDayFound" -eq 1 ] ; then
    echo "Invalid Schedule: Contains non-allowed teaching days (Only S, M, T, W, Th are valid)."
else
    echo "Valid Schedule: All teaching days are allowed."
fi
echo "===================================================="
echo "Exit Schedule Validator System ........!"

