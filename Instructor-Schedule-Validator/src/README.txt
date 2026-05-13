Linux Lab – Second Project (Python)

Team Members:
Student 1:
Name: Abd-Alrahman Naser
ID: 1231814
Section: 4

Student 2:
Name: Wadee Owais
ID: 1230482
Section: 4
ــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــ

1. Introduction

   This project implements an Instructor Schedule Validator using Python. The program reads three input text files (codes.txt, instructorCodes.txt, and schedule.txt), creates instructor objects, and applies multiple validation rules to            determine whether each instructor’s schedule is VALID or INVALID.
   For each instructor, the output includes a clear rule-by-rule status message.

All three text files must be located in the same directory as the Python script:

* codes.txt            (valid course codes)
* instructorCodes.txt  (instructor allowed/preferred courses)
* schedule.txt         (weekly schedule)

ـــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــ
2) File Formats

(1) codes.txt
Each line contains a single valid course/lab code. Example:
ENCS334
ENCS324
ENCS212
ENCS333
ENCS314
ENCS311

(2) instructorCodes.txt
Each line maps an instructor ID to the list of courses they are allowed to teach (separated by semicolons). Example:
120345: ENCS334; ENCS324; ENCS212; ENCS333; ENCS312; ENCS311
1221669: ENCS334; ENCS533; ENCS233; ENCS432

(3) schedule.txt
Each instructor block begins with a header line:
ID, FirstName, LastName

Then 5 lines follow, one per day, in this format:
S  | [start-end]CODE; [start-end]CODE; ...
M  | ...
T  | ...
W  | ...
Th | ...

Example:
120345, Ahmad, Saleh
S  | [9-10]ENCS334; [10-11]ENCS324; [13-15]OH;
M  | [9-10]ENCS334; [10-11]ENCS324; [13-15]OH;
T  | [8-11]ENCS212; [11-12]OH; [13-15]ENCS333;
W  | [9-10]ENCS334; [10-11]ENCS324; [13-15]OH;
Th | [11-12]OH; [13-15]ENCS333;

1221669, Wadee, Naser
S  | [8-11]ENCS314; [10-12]ENCS53; [13-14]OH
M  |
T  |
W  | [10-9]ENCS535
Th | [8-11]ENCS314;[11-12]ENCS534;[12-13]ENCS532; [10-12]OH

ـــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــ
3) Implemented Features (Validation Rules)

For each instructor, the program validates the schedule using these checks:

A) Teaching Load Calculation

* Calculates total teaching hours (courses/labs only).
* Calculates total office hours (OH) separately.

B) Teaching Load Validation

* Lecture hours must be between 12 and 18.
* Office hours must be at least 50% of lecture hours.

C) Teaching Days Distribution

* Teaching must occur across at least four distinct instructional days.

D) Time Conflict Detection

* Detects overlapping time intervals within the same day.

E) Consecutive Teaching Rule

* No more than two courses/labs can be scheduled consecutively without a break.

F) Allowed Teaching Days Validation

* Ensures teaching day grouping follows either:
  (S, M, W) or (T, Th)

G) Course/Lab Code Validation

* Ensures every scheduled course/lab code exists in codes.txt.

H) Instructor Preference Validation

* Ensures assigned courses/labs match the instructor’s allowed list in instructorCodes.txt.

I) Lab Rule

* A lab course whose second digit is 1 may appear only once per week.

Output Note:

* Each rule prints a clear message indicating PASS/FAIL (VALID/INVALID).
ـــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــ
4) Assumptions

* Course codes contain digits, and the second digit is used to classify lab vs lecture.
* Time ranges follow the format [start-end] using a 12-hour style.
* OH represents office hours and is excluded from course-specific rules.
