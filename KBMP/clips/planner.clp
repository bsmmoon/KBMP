; ; Module planner CLIPS implementation
; ; TEMPLATES

; ; Module
(deftemplate  module    "Module Information"
    (slot code
        (type SYMBOL))
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
        (type SYMBOL)   
        (default none))
    (slot status
        (type SYMBOL)   
        (default none)))

; ; Sample modules
(deffacts sample-modules
    (module (code CS1101S) (name "Programming Methodology") (MC 5) (prerequisites none))
    (module (code CS1010) (name "Programming Methodology") (MC 4) (prerequisites none))
    (module (code CS1020) (name "Data Structures and Algorithms I") (MC 4) (prerequisites CS1010))
    (module (code CS2010) (name "Data Structures and Algorithms II") (MC 4) (prerequisites CS1020))
    (module (code CS2020) (name "Data Structures and Algorithms Accelerated") (MC 6) (prerequisites CS1010))
    (module (code CS2020) (name "Data Structures and Algorithms Accelerated") (MC 6) (prerequisites CS1101S))
    (module (code CS2100) (name "Computer Organisation") (MC 4) (prerequisites CS1010))
    (module (code CS2100) (name "Computer Organisation") (MC 4) (prerequisites CS1101S)))

; ;
(deftemplate flag
    (slot type (type SYMBOL)))

(deftemplate test-template
    (slot slot1 (type SYMBOL))
    (slot slot2 (type SYMBOL))
    (slot slot3 (type SYMBOL)))

; ; RULES

(defrule mark-available "mark modules without prerequisites as available"
    ?module <- (module (code ?code) (prerequisites none) (status none))
    =>
    (printout t "Marked as available: " ?code crlf)
    (modify ?module (status available)))

; ; TESTS
(deffacts test-facts
    (test-template (slot1 true) (slot2 false) (slot3 wow))
    (test-template (slot1 true) (slot2 wow) (slot3 false))
    (test-template (slot1 false) (slot2 true) (slot3 wow)))

(defrule call-test-facts
    ?flag-fact <- (flag (type test))
    (test-template (slot1 true) (slot2 ?x))
    =>
    (printout t "test: " ?x crlf)
    (retract ?flag-fact))
