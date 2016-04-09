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
        (type SYMBOL))
    (slot classification
        (type SYMBOL))
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
        (default none)))

(deftemplate focus
    (slot name (type STRING))
    (multislot primaries (type STRING))
    (multislot electives (type STRING))
    (multislot unrestricted-electives (type STRING))
    (slot status (type SYMBOL) (default none)))

; ; Sample modules
 (deffacts sample-modules
; ;     (module (code "CS1101S") (name "Programming Methodology") (MC 5) (prerequisites ""))
; ;     (module (code "CS1010") (name "Programming Methodology") (MC 4) (prerequisites ""))
; ;     (module (code "CS1231") (name "Discrete Structures") (MC 4) (prerequisites ""))
; ;     (module (code "CS1020") (name "Data Structures and Algorithms I") (MC 4) (prerequisites "CS1010"))
; ;     (module (code "CS2010") (name "Data Structures and Algorithms II") (MC 4) (prerequisites "CS1020"))
; ;     (module (code "CS2020") (name "Data Structures and Algorithms Accelerated") (MC 6) (prerequisites "CS1010"))
; ;     (module (code "CS2020") (name "Data Structures and Algorithms Accelerated") (MC 6) (prerequisites "CS1101S"))
     (module (code "CS2100") (name "Computer Organisation") (MC 4) (prerequisites "CS1010"))
     (module (code "CS2100") (name "Computer Organisation") (MC 4) (prerequisites "CS1101S")))

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

; ; MODULES
(defmodule RANK (import MAIN ?ALL))
(defmodule SELECT (import MAIN ?ALL))

; ; RULES
; ; Ranking modules for availability

; ; Wanted modules
(defrule RANK::mark-wanted "mark wanted modules"
    ?module <- (module (code ?code1) (want NONE))
    (want ?code2)
    (test(eq ?code1 ?code2))
    =>
    (printout t "Marking module " ?code1 " as wanted" crlf)
    (modify ?module (want yes)))

; ; Unwanted modules
(defrule RANK::mark-unwanted "mark unwanted modules"
    ?module <- (module (code ?code1) (want NONE))
    (dontwant ?code2)
    (test(eq ?code1 ?code2))
    =>
    (printout t "Marking module " ?code1 " as unwanted" crlf)
    (modify ?module (want no)))

; ; Modules without prerequisites
(defrule RANK::mark-available-no-prerequisites "mark modules without prerequisites as available"
    ?module <- (module (code ?code) (prerequisites "") (status none) (want ~no))
    =>
    (printout t "Marking module " ?code " as available" crlf)
    (modify ?module (status available)))

; ; Modules with single prerequisite met
(defrule RANK::mark-available-prerequisite-met "mark modules with single prerequisite met as available"
    ?module <- (module (code ?code) (prerequisites ?prereq) (status none) (want ~no))
    (module (status planned) (code ?plannedcode))
    (test(eq ?prereq ?plannedcode))
    =>
    (printout t "Marking module " ?code " as available" crlf)
    (modify ?module (status available)))

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
