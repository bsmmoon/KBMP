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
    (slot status
        (type SYMBOL)   
        (default none)))

; ; Sample modules
(deffacts sample-modules
    (module (code "CS1101S") (name "Programming Methodology") (MC 5) (prerequisites ""))
    (module (code "CS1010") (name "Programming Methodology") (MC 4) (prerequisites ""))
    (module (code "CS1231") (name "Discrete Structures") (MC 4) (prerequisites ""))
    (module (code "CS1020") (name "Data Structures and Algorithms I") (MC 4) (prerequisites "CS1010"))
    (module (code "CS2010") (name "Data Structures and Algorithms II") (MC 4) (prerequisites "CS1020"))
    (module (code "CS2020") (name "Data Structures and Algorithms Accelerated") (MC 6) (prerequisites "CS1010"))
    (module (code "CS2020") (name "Data Structures and Algorithms Accelerated") (MC 6) (prerequisites "CS1101S"))
    (module (code "CS2100") (name "Computer Organisation") (MC 4) (prerequisites "CS1010"))
    (module (code "CS2100") (name "Computer Organisation") (MC 4) (prerequisites "CS1101S")))

; ; FUNCTIONS
(deffunction assert-taken (?x)
    (assert (taken ?x)))

(deffunction assert-want (?x)
    (assert (want ?x)))

(deffunction assert-dontwant (?x)
    (assert (dontwant ?x)))

(deffunction assert-selected (?x ?y)
    (assert (selected ?x ?y)))

(deffunction assert-planned (?x)
    (assert (planned ?x)))

; ; MODULES
(defmodule RANK (import MAIN ?ALL))
(defmodule SELECT (import MAIN ?ALL))

; ; RULES
; ; Ranking modules


(defrule RANK::mark-available-no-prequisite "mark modules without prerequisites as available"
    ?module <- (module (code ?code) (prerequisites "") (status none))
    =>
    (modify ?module (status available)))

(defrule RANK::mark-available-prequisite-met "mark modules with prerequisites met as available"
    ?module <- (module (code ?code) (prerequisites ?prereq) (status none))
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
