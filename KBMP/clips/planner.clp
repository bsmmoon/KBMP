; ; Module planner CLIPS implementation
; ; TEMPLATES

; ; Module
(deftemplate  module    "Module Information"
    (slot code
        (type SYMBOL))  
    (slot title
        (type STRING)   
        (default ?DERIVE))  
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
    (module (code CS1101S) (title "Programming Methodology") (MC 5) (prerequisites none))
    (module (code CS1010) (title "Programming Methodology") (MC 4) (prerequisites none))
    (module (code CS1020) (title "Data Structures and Algorithms I") (MC 4) (prerequisites CS1010))
    (module (code CS2010) (title "Data Structures and Algorithms II") (MC 4) (prerequisites CS1020))
    (module (code CS2020) (title "Data Structures and Algorithms Accelerated") (MC 6) (prerequisites CS1010))
    (module (code CS2020) (title "Data Structures and Algorithms Accelerated") (MC 6) (prerequisites CS1101S))
    (module (code CS2100) (title "Computer Organisation") (MC 4) (prerequisites CS1010))
    (module (code CS2100) (title "Computer Organisation") (MC 4) (prerequisites CS1101S)))


; ; RULES

(defrule mark-available "mark modules without prerequisites as available"
    ?module <- (module (code ?code) (prerequisites none) (status none))
=>
    (printout t "Marked as available: " ?code crlf)
    (modify ?module (status available))
)