; ; Module planner CLIPS implementation
(defmodule MAIN
    (export ?ALL))
; ; TEMPLATES

; ; Module
(deftemplate module    "Module Information"
    (slot code
        (type STRING))
    (slot name
        (type STRING)
        (default ?DERIVE))
    (slot prefix
        (type STRING))
    (slot level
        (type INTEGER))
    (slot rest
        (type STRING))
    (multislot classification
        (type SYMBOL))
    (multislot focusarea
        (type STRING))
    (slot MC
        (type INTEGER)  
        (default 4))
    ; ; Need to have logical connectors for prerequisites (AND, OR)
    ; ; Temporarily multislot prerequisites includes modules connected with AND
    ; ; Multiple instances of modules are created to represent OR relations in prerequisites
    ; ; And example would be CS2020
    ; ; Potentially useful info: http://stackoverflow.com/questions/12695176/clips-multifield-slots
    (multislot prerequisites
        (type STRING)
        (default ""))
    (slot want
        (type SYMBOL)
        (allowed-symbols yes no NONE)
        (default NONE))
    (slot status
        (type SYMBOL)   
        (default none))
    (slot score
        (type INTEGER)   
        (default -99)))

(deftemplate focus
    (slot name (type STRING))
    (multislot primaries (type STRING))
    (multislot electives (type STRING))
    (multislot unrestricted-electives (type STRING))
    (slot status (type SYMBOL) (default none)))

; ; Sample modules
; ; (deffacts sample-modules
; ;     (module (code "CS1101S") (name "Programming Methodology") (MC 5) (prerequisites ""))
; ;     (module (code "CS1010") (name "Programming Methodology") (MC 4) (prerequisites ""))
; ;     (module (code "CS1231") (name "Discrete Structures") (MC 4) (prerequisites ""))
; ;     (module (code "CS1020") (name "Data Structures and Algorithms I") (MC 4) (prerequisites "CS1010"))
; ;     (module (code "CS2010") (name "Data Structures and Algorithms II") (MC 4) (prerequisites "CS1020"))
; ;     (module (code "CS2020") (name "Data Structures and Algorithms Accelerated") (MC 6) (prerequisites "CS1010"))
; ;     (module (code "CS2020") (name "Data Structures and Algorithms Accelerated") (MC 6) (prerequisites "CS1101S"))
; ;     (module (code "CS2100") (name "Computer Organisation") (MC 4) (prerequisites "CS1010"))
; ;     (module (code "CS2100") (name "Computer Organisation") (MC 4) (prerequisites "CS1101S")))

; ; FUNCTIONS
(deffunction assert-taken (?x)
    (assert (taken ?x)))

(deffunction assert-want (?x)
    (assert (want ?x)))

(deffunction assert-dontwant (?x)
    (assert (dontwant ?x)))

(deffunction assert-selected (?x)
    (assert (planned ?x)))

(deffunction assert-focus-on (?x)
    (assert (focus-on ?x)))

(deffunction count-level-one ()
    (length$ (find-all-facts ((?f module)) (and (eq ?f:level 1) (or (eq ?f:status planned) (eq ?f:status taken))))))

(deffunction count-available ()
    (length$ (find-all-facts ((?f module)) (eq ?f:status available))))


; ; MODULES
(defmodule RANK (import MAIN ?ALL))
(defmodule SELECT (import MAIN ?ALL))

; ; RULES

; ; RANK

; ; ---------------
; ; USER PREFERENCE
; ; ---------------

; ; Wanted modules
(defrule RANK::mark-wanted "mark wanted modules"
    ?module <- (module (code ?code1) (want NONE))
    (want ?code2)
    (test(eq ?code1 ?code2))
    =>
    (printout t "Marking module " ?code1 " as wanted" crlf)
    (modify ?module (want yes) (score -99)))

; ; Unwanted modules
(defrule RANK::mark-unwanted "mark unwanted modules"
    ?module <- (module (code ?code1) (want NONE))
    (dontwant ?code2)
    (test(eq ?code1 ?code2))
    =>
    (printout t "Marking module " ?code1 " as unwanted" crlf)
    (modify ?module (want no) (score -99)))

; ; ----------------
; ; STATUS AVAILABLE
; ; ----------------

; ; Modules Available, without prerequisites, level 1, salience 4, no limit
(defrule RANK::mark-available-no-prerequisites-level-1 "mark modules without prerequisites as available"
    (declare (salience 4))
    ?module <- (module (code ?code) (prerequisites "") (status none) (want ~no) (level 1))
    =>
    (printout t "Module " ?code " available." crlf)
    (printout t "Total available: " (count-available) "Level 1 planned/taken: " (count-level-one) crlf)
    (modify ?module (status available))
    )

; ; Modules Available, without prerequisites, level 2, salience 3, check total limit of 15
(defrule RANK::mark-available-no-prerequisites-level-2 "mark modules without prerequisites as available"
    (declare (salience 3))
    ?module <- (module (code ?code) (prerequisites "") (status none) (want ~no) (level 2))
    =>
    (if (< (count-available) 15)
        then
        (printout t "Module " ?code " available." crlf)
        (modify ?module (status available))
        ; ; else
        ; ; (printout t "Total available reached max " (count-available) crlf)
        )
    )

; ; Modules Available, without prerequisites, level 3, salience 2, check total limit of 15
(defrule RANK::mark-available-no-prerequisites-level-3 "mark modules without prerequisites as available"
    (declare (salience 2))
    ?module <- (module (code ?code) (prerequisites "") (status none) (want ~no) (level 3))
    =>
    (if (< (count-available) 15)
        then
        (printout t "Module " ?code " available." crlf)
        (modify ?module (status available))
        ; ; else
        ; ; (printout t "Total available reached max " (count-available) crlf)
        )
    )

; ; Modules Available, without prerequisites, level 3 above, salience 0, limit total to 15
(defrule RANK::mark-available-no-prerequisites-level-3-higher "mark modules without prerequisites as available"
    (declare (salience 0))
    ?module <- (module (code ?code) (prerequisites "") (status none) (want ~no) (level ?level&:(> ?level 3)))
    =>
    (if (< (count-available) 15)
        then
        (printout t "Module " ?code " available." crlf)
        (modify ?module (status available))
        ; ; else
        ; ; (printout t "Total available reached max " (count-available) crlf)
        )
    )

; ; Modules Available, with single prerequisite met, no limit
(defrule RANK::mark-available-1-prerequisite-met "mark modules with single prerequisite met as available"
    (declare (salience 5))
    ?module <- (module (code ?code) (prerequisites ?prereq1) (status none) (want ~no))
    (module (status planned|taken) (code ?prereq1))
    =>
    (printout t "Module " ?code " available as single prereq met" crlf)
    (modify ?module (status available)))

; ; Modules Available, with 2 prerequisites met, no limit
(defrule RANK::mark-available-2-prerequisites-met "mark modules with single prerequisite met as available"
    ?module <- (module (code ?code) (prerequisites ?prereq1 ?prereq2) (status none) (want ~no))
    (module (status planned|taken) (code ?prereq1))
    (module (status planned|taken) (code ?prereq2))
    =>
    (printout t "Module " ?code " available as 2 prereqs met" crlf)
    (modify ?module (status available)))

; ; Modules Available, with 3 prerequisites met, no limit
(defrule RANK::mark-available-3-prerequisites-met "mark modules with single prerequisite met as available"
    ?module <- (module (code ?code) (prerequisites ?prereq1 ?prereq2 ?prereq3) (status none) (want ~no))
    (module (status planned|taken) (code ?prereq1))
    (module (status planned|taken) (code ?prereq2))
    (module (status planned|taken) (code ?prereq3))
    =>
    (printout t "Module " ?code " available as 3 prereqs met" crlf)
    (modify ?module (status available)))

; ; Modules Available, with 4 prerequisites met, no limit
(defrule RANK::mark-available-4-prerequisites-met "mark modules with single prerequisite met as available"
    ?module <- (module (code ?code) (prerequisites ?prereq1 ?prereq2 ?prereq3 ?prereq4) (status none) (want ~no))
    (module (status planned|taken) (code ?prereq1))
    (module (status planned|taken) (code ?prereq2))
    (module (status planned|taken) (code ?prereq3))
    (module (status planned|taken) (code ?prereq4))
    =>
    (printout t "Module " ?code " available as 4 prereqs met" crlf)
    (modify ?module (status available)))

(deffacts RANK::preclusions
(preclusion "CS1010" "CS1010S" "CS1010E" "CS1010R" "CS1010J" "CS1101S" "CS1010X"))

; ; --------------------
; ; STATUS NOT-AVAILABLE
; ; --------------------

; ; Module precluded when preclusion met
(defrule RANK::mark-not-available-preclusion-met
    (module (code ?code1) (status ?status))
    (test(or (eq ?status planned) (eq ?status taken)))
    (preclusion $?preclusionlist)
    (test (member$ ?code1 $?preclusionlist))
    ?precludedmodule <- (module (code ?code2) (status available))
    (test (member$ ?code2 $?preclusionlist))
    =>
    (modify ?precludedmodule (status not-available))
    (printout t "Module " ?code2 " precluded " crlf))

; ; Copies of same modules with different prerequisites
(defrule RANK::mark-not-available-different-prereq
    (module (code ?code) (status planned))
    ?copy <- (module (code ?code) (status none))
    =>
    (modify ?copy (status not-available))
    (printout t "Module copy " ?code " marked not available." crlf))

; ; -------------
; ; ASSIGN SCORE
; ; -------------

(deffunction RANK::calscore (?level ?want)
    (if (eq ?want yes) 
    then 
        (return (+ (- 5 ?level) 5))
    else
        (return (- 5 ?level))
    ))

(defrule RANK::assign-score
    ?module <- (module (code ?code) (status available) (level ?level) (want ?want&~no) (score -99))
    =>
    (bind ?score (calscore ?level ?want))
    (modify ?module (score ?score))
    (printout t "Module " ?code " score: " ?score crlf))

; ; SELECT
; ; Selecting modules
(defrule SELECT::mark-planned "mark modules that the user plan to take"
    ?module <- (module (status available) (code ?code1))
    (planned ?code2)
    (test(eq ?code1 ?code2))
    =>
    (printout t "Marking module " ?code1 " as planned" crlf)
    (modify ?module (status planned)))

(defrule SELECT::mark-taken "mark modules that the user have already taken"
    ?module <- (module (status ~taken) (code ?code1))
    (taken ?code2)
    (test(eq ?code1 ?code2))
    =>
    (printout t "Marking module " ?code1 " as taken" crlf)
    (modify ?module (status taken)))

(defrule SELECT::mark-focus "mark focus area that the user plan to take"
    ?focus <- (focus (status none) (name ?name1))
    (focus-on ?name2)
    (test(eq ?name1 ?name2))
    =>
    (printout t "Marking focus area " ?name1 " as planned" crlf)
    (modify ?focus (status planned)))
