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
        (default -99))
    (slot recommend
        (type SYMBOL)
        (allowed-symbols yes no NONE)
        (default NONE)))

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

(deffunction assert-primaryfocus ($?x)
    (assert (primaryfocus $?x)))

(deffunction assert-electivefocus ($?x)
    (assert (electivefocus $?x)))

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
    ; ; (printout t "Total available: " (count-available) "Level 1 planned/taken: " (count-level-one) crlf)
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

; ; ---------------
; ; PREREQ HANDLING
; ; ---------------

; ; Modules Available, with single prerequisite met, no limit
(defrule RANK::mark-available-1-prerequisite-met "mark modules with single prerequisite met as available"
    (declare (salience 5))
    ?module <- (module (code ?code) (prerequisites ?prereq1) (status none) (want ~no))
    (module (status planned|taken|equivalenttaken) (code ?prereq1))
    (not (exists (module (status available) (code ?code))))
    =>
    (printout t "Module " ?code " available as single prereq met" crlf)
    (modify ?module (status available)))

; ; Modules Available, with 2 prerequisites met, no limit
(defrule RANK::mark-available-2-prerequisites-met "mark modules with single prerequisite met as available"
    ?module <- (module (code ?code) (prerequisites ?prereq1 ?prereq2) (status none) (want ~no))
    (module (status planned|taken|equivalenttaken) (code ?prereq1))
    (module (status planned|taken|equivalenttaken) (code ?prereq2))
    (not (exists (module (status available) (code ?code))))
    =>
    (printout t "Module " ?code " available as 2 prereqs met" crlf)
    (modify ?module (status available)))

; ; Modules Available, with 3 prerequisites met, no limit
(defrule RANK::mark-available-3-prerequisites-met "mark modules with single prerequisite met as available"
    ?module <- (module (code ?code) (prerequisites ?prereq1 ?prereq2 ?prereq3) (status none) (want ~no))
    (module (status planned|taken|equivalenttaken) (code ?prereq1))
    (module (status planned|taken|equivalenttaken) (code ?prereq2))
    (module (status planned|taken|equivalenttaken) (code ?prereq3))
    (not (exists (module (status available) (code ?code))))
    =>
    (printout t "Module " ?code " available as 3 prereqs met" crlf)
    (modify ?module (status available)))

; ; Modules Available, with 4 prerequisites met, no limit
(defrule RANK::mark-available-4-prerequisites-met "mark modules with single prerequisite met as available"
    ?module <- (module (code ?code) (prerequisites ?prereq1 ?prereq2 ?prereq3 ?prereq4) (status none) (want ~no))
    (module (status planned|taken|equivalenttaken) (code ?prereq1))
    (module (status planned|taken|equivalenttaken) (code ?prereq2))
    (module (status planned|taken|equivalenttaken) (code ?prereq3))
    (module (status planned|taken|equivalenttaken) (code ?prereq4))
    (not (exists (module (status available) (code ?code))))
    =>
    (printout t "Module " ?code " available as 4 prereqs met" crlf)
    (modify ?module (status available)))


; ; -----------------
; ; EQUIVALENCE FACTS
; ; -----------------

; ; Equivalence facts
; ; Read as: If the second entry is taken, the first entry is considered taken
(deffacts RANK::equivalence-facts
(equivalence "CS1010" "CG1101")
(equivalence "CS1010" "CS1010E")
(equivalence "CS1010" "CS1010FC")
(equivalence "CS1010" "CS1010S")
(equivalence "CS1010" "CS1101")
(equivalence "CS1010" "CS1101C")
(equivalence "CS1010" "CS1101S")
(equivalence "CS1010" "CS1010J")
(equivalence "CS1010" "CG1101")
(equivalence "CS1010" "CS1010X")
(equivalence "CS1020" "CG1102")
(equivalence "CS1020" "CG1103")
(equivalence "CS1020" "CS1020E")
(equivalence "CS1020" "CS1102")
(equivalence "CS1020" "CS1102C")
(equivalence "CS1020" "CS1102S")
(equivalence "CS1020" "CS2020")
(equivalence "CS1231" "MA1100")
(equivalence "CS2010" "CG1102")
(equivalence "CS2010" "CS2020")
(equivalence "CS2103" "CS2103T")
(equivalence "ST2131" "ST2334")
(equivalence "MA1301" "H2Math")
(equivalence "PC1221" "H2Physics")
(equivalence "PC1222" "H2Physics"))

; ; ---------------------
; ; EQUIVALENCE IN PREREQ
; ; ---------------------
; ; Module marked as equivalenttaken when equivalence met
(defrule RANK::mark-equivalenttaken-equivalence-met-planned
    ?equivalenttakenmodule <- (module (code ?code1) (status ?status&~planned&~taken&~equivalenttaken))
    (module (code ?code2) (status planned))
    (equivalence ?code1 ?code2)
    =>
    (modify ?equivalenttakenmodule (status equivalenttaken))
    (printout t "Module " ?code1 " equivalent taken due to " ?code1 crlf))

(defrule RANK::mark-equivalenttaken-equivalence-met-taken
    ?equivalenttakenmodule <- (module (code ?code) (status ?status&~planned&~taken&~equivalenttaken))
    (taken ?taken)
    (equivalence ?code ?taken)
    =>
    (modify ?equivalenttakenmodule (status equivalenttaken))
    (printout t "Module " ?code " equivalent taken due to " ?taken crlf))

; ; ----------------
; ; PRECLUSION FACTS
; ; ----------------

; ; Standard list of preclusions, with first element as the default choice for module recommendation
(deffacts RANK::standard-preclusions
(preclusion "CS1231" "MA1100")
(preclusion "CS1231R" "MA1100")
(preclusion "CS2100" "CS1104")
(preclusion "CS2100R" "CS1104")
(preclusion "CS2108" "CS3246")
(preclusion "CS2309" "CS2305S")
(preclusion "CS3201" "CS3215")
(preclusion "CS3202" "CS3215")
(preclusion "CS3219" "CS3213")
(preclusion "CS3283" "CS4201" "CS4202" "CS4203" "CS4204")
(preclusion "CS3284" "CS4201" "CS4202" "CS4203" "CS4204")
(preclusion "CS4350" "CS4203" "CS4204")
(preclusion "MA1301" "H2Math" "MA1301X")
(preclusion "MA1101R" "EG1401" "EG1402" "MA1101" "MA1311" "MA1506" "MA1508")
(preclusion "MA1521" "MA1102R" "MA1312" "MA1505" "MA1507" "MA2501")
(preclusion "PC1221" "H2Physics" "PC1221X")
(preclusion "PC1222" "H2Physics" "PC1222X")
(preclusion "ST2334" "ST2131" "MA2216" "CE2407")
(preclusion "CS3226" "CP3101B")
(preclusion "CS3242" "CS4342")
(preclusion "CS3246R" "CS4341")
(preclusion "CS3247" "CS4213")
(preclusion "CS4340" "CS5245")
(preclusion "CS5230" "CS4230")
(preclusion "MA1301X" "PC1221X" "PC1141" "PC1142" "PC1431" "PC1431FC" "PC1431X" "PC1221" "PC1221FC")
(preclusion "PC1222X" "PC1222" "PC1143" "PC1144" "PC1432" "PC1432X"))

; ; Situational preclusions as rules to allow user preference
(defrule RANK::normal-math-preclusion "preclusions for students with normal math background"
(normalmath)
=>
(assert (preclusion "CS1010" "CG1101" "CS1010E" "CS1010FC" "CS1010S" "CS1101" "CS1101C" "CS1101S" "CS1010J" "CS1010R" "CS1010X"))
(assert (preclusion "CS1020" "CG1102" "CG1103" "CS1020E" "CS1102" "CS1102C" "CS1102S" "CS2020"))
(assert (preclusion "CS2010" "CG1102" "CS1102" "CS1102C" "CS1102S" "CS2020" "CS2010R")))

(defrule RANK::good-math-preclusion "preclusions for students with good math background"
(goodmath)
=>
(assert (preclusion "CS1101S" "CG1101" "CS1010" "CS1010E" "CS1010FC" "CS1010S" "CS1010X" "CS1101" "CS1101C" "CS1010J" "CS1010R"))
(assert (preclusion "CS2020" "CG1102" "CG1103" "CS1020" "CS1020E" "CS2010" "CS1102" "CS1102C" "CS1102S")))

(defrule RANK::communication-exemption-preclusion "preclusions for students with communication module exempted"
(commexempted)
=>
(assert (preclusion "CS2103" "CS2101" "CS2103T")))

(defrule RANK::communication-normal-preclusion "preclusions for students without communication module exempted"
(commnotexempted)
=>
(assert (preclusion "CS2103T" "CS2103")))

; ; -------------------------
; ; PRECLUSION RECOMMENDATION
; ; -------------------------

; ; Set module recommend field to yes if it is the default choice for a preclusion list
(defrule RANK::set-recommendation-yes
    ?module <- (module (code ?code) (recommend NONE) (status available))
    (preclusion $?preclusionlist)
    (test (member$ ?code ?preclusionlist))
    (test (eq ?code (nth$ 1 ?preclusionlist)))
    =>
    (modify ?module (recommend yes))
    (printout t "Module " ?code " recommended based on preclusion list" crlf))

; ; Set module recommend field to no if it is not the default choice for a preclusion list
(defrule RANK::set-recommendation-no
    ?module <- (module (code ?code) (recommend NONE) (status available))
    (preclusion $?preclusionlist)
    (test (member$ ?code ?preclusionlist))
    (test (neq ?code (nth$ 1 ?preclusionlist)))
    =>
    (modify ?module (recommend no))
    (printout t "Module " ?code " NOT recommended based on preclusion list" crlf))


; ; --------------------
; ; STATUS NOT-AVAILABLE
; ; --------------------

; ; Module precluded when preclusion met
(defrule RANK::mark-not-available-preclusion-met-planned
    (module (code ?code1) (status planned))
    (preclusion $?preclusionlist)
    (test (member$ ?code1 ?preclusionlist))
    ?precludedmodule <- (module (code ?code2) (status available))
    (test (member$ ?code2 ?preclusionlist))
    =>
    (modify ?precludedmodule (status not-available))
    (printout t "Module " ?code2 " precluded due to " ?code1 crlf))

(defrule RANK::mark-not-available-preclusion-met-taken
    (taken ?taken)
    (preclusion $?preclusionlist)
    (test (member$ ?taken ?preclusionlist))
    ?precludedmodule <- (module (code ?code2) (status available))
    (test (member$ ?code2 ?preclusionlist))
    =>
    (modify ?precludedmodule (status not-available))
    (printout t "Module " ?code2 " precluded due to " ?taken crlf))

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

(deffunction RANK::calscore (?level ?want ?primaries ?electives ?code ?prefix ?MC)
    ; ; Default score inverse to module level
    (bind ?score (- 5 ?level))
    ; ; Add score for wanted modules
    (if (eq ?want yes)
    then 
        (bind ?score (+ 10 ?score)))
    ; ; Add score for focus area primary modules
    (if (member$ ?code ?primaries)
    then
        (bind ?score (+ 8 ?score)))
    ; ; Add score for focus area elective modules
    (if (member$ ?code ?electives)
    then
        (bind ?score (+ 3 ?score)))
    ; ; Reduce score for CS research-based 1MC modules
    (if (and (eq ?prefix "CS") (eq ?MC 1))
    then
        (bind ?score (- ?score 3)))
    return ?score)

(defrule RANK::assign-score
    ?module <- (module (code ?code) (status available) (level ?level) (prefix ?prefix) (MC ?MC) (want ?want&~no) (score -99))
    (primaryfocus $?primaries)
    (electivefocus $?electives)
    =>
    (bind ?score (calscore ?level ?want ?primaries ?electives ?code ?prefix ?MC))
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
