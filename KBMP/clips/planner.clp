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
        (default NONE))
    (slot semester
        (type SYMBOL)
        (default BOTH)))

(deftemplate focus
    (slot name (type STRING))
    (multislot primaries (type STRING))
    (multislot electives (type STRING))
    (multislot unrestricted-electives (type STRING))
    (slot status (type SYMBOL) (default none)))

(deftemplate current-semester
    (slot number (type INTEGER)))

(deftemplate skip-semester
    (slot number 
        (type INTEGER))
    (slot module 
        (type STRING)
        (default "")))

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

(deffunction count-planned-and-taken ()
    (length$ (find-all-facts ((?f module)) (or (eq ?f:status planned) (eq ?f:status taken)))))

(deffunction count-available ()
    (length$ (find-all-facts ((?f module)) (eq ?f:status available))))

(deffunction increment-semester ()
    (do-for-fact ((?x current-semester))
        (assert (current-semester (number (+ ?x:number 1))))
        (retract ?x)))

; ; MODULES
(defmodule RANK (import MAIN ?ALL))
(defmodule SELECT (import MAIN ?ALL))

; ; FACTS

; ; All compulsory project modules
(deffacts RANK::all-projects
(allprojects "CS2103T" "CS2101" "CS3201" "CS3202" "CS3281" "CS3282" "CS3283" "CS3284"))


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

; ; ---------------------
; ; MARK STATUS AVAILABLE
; ; ---------------------

; ; NO PREREQUISITES

; ; Modules Available, fake modules from other faculties, without prerequisites, level 0, salience 4, no limit
(defrule RANK::mark-available-no-prerequisites-level-0 "mark level 0 modules without prerequisites as available"
    (declare (salience 4))
    ?module <- (module (code ?code) (prerequisites "") (status none) (want ~no) (level 0))
    =>
    (printout t "Module " ?code " available." crlf)
    (modify ?module (status available))
    )

; ; Modules Available, without prerequisites, level 1, salience 4, no limit
(defrule RANK::mark-available-no-prerequisites-level-1 "mark level 1 modules without prerequisites as available"
    (declare (salience 4))
    ?module <- (module (code ?code) (prerequisites "") (status none) (want ~no) (level 1))
    =>
    (printout t "Module " ?code " available." crlf)
    (modify ?module (status available))
    )

; ; Modules Available, without prerequisites, level 2, salience 3, check total limit of 15
(defrule RANK::mark-available-no-prerequisites-level-2 "mark level 2 modules without prerequisites as available"
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
(defrule RANK::mark-available-no-prerequisites-level-3 "mark level 3 modules without prerequisites as available"
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
(defrule RANK::mark-available-no-prerequisites-level-3-higher "mark level 3 above modules without prerequisites as available"
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

(defrule RANK::mark-available-next-sem-1 "mark modules that is available but not offered this sem as available-next-sem"
    ?module <- (module (status available) (semester ONE) (code ?code))
    (current-semester (number 2|4|6|8))
    =>
    (printout t "Mark " ?code " as available-next-sem 1." crlf)
    (modify ?module (status available-next-sem)))

(defrule RANK::mark-available-next-sem-2 "mark modules that is available but not offered this sem as available-next-sem"
    ?module <- (module (status available) (semester TWO) (code ?code))
    (current-semester (number 1|3|5|7))
    =>
    (printout t "Mark " ?code " as available-next-sem 2." crlf)
    (modify ?module (status available-next-sem)))

(defrule RANK::mark-available-this-sem-1 "toggle modules marked as available-next-sem to available"
    ?module <- (module (status available-next-sem) (semester ONE) (code ?code))
    (current-semester (number 1|3|5|7))
    =>
    (printout t "Module " ?code " available again from available-next-sem." crlf)
    (modify ?module (status available)))

(defrule RANK::mark-available-this-sem-2 "toggle modules marked as available-next-sem to available"
    ?module <- (module (status available-next-sem) (semester TWO) (code ?code))
    (current-semester (number 2|4|6|8))
    =>
    (printout t "Module " ?code " available again from available-next-sem." crlf)
    (modify ?module (status available)))

; ; HANDLING MODULES WITH PREREQS

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
(defrule RANK::mark-available-2-prerequisites-met "mark modules with 2 prerequisites met as available"
    ?module <- (module (code ?code) (prerequisites ?prereq1 ?prereq2) (status none) (want ~no))
    (module (status planned|taken|equivalenttaken) (code ?prereq1))
    (module (status planned|taken|equivalenttaken) (code ?prereq2))
    (not (exists (module (status available) (code ?code))))
    =>
    (printout t "Module " ?code " available as 2 prereqs met" crlf)
    (modify ?module (status available)))

; ; Modules Available, with 3 prerequisites met, no limit
(defrule RANK::mark-available-3-prerequisites-met "mark modules with 3 prerequisites met as available"
    ?module <- (module (code ?code) (prerequisites ?prereq1 ?prereq2 ?prereq3) (status none) (want ~no))
    (module (status planned|taken|equivalenttaken) (code ?prereq1))
    (module (status planned|taken|equivalenttaken) (code ?prereq2))
    (module (status planned|taken|equivalenttaken) (code ?prereq3))
    (not (exists (module (status available) (code ?code))))
    =>
    (printout t "Module " ?code " available as 3 prereqs met" crlf)
    (modify ?module (status available)))

; ; Modules Available, with 4 prerequisites met, no limit
(defrule RANK::mark-available-4-prerequisites-met "mark modules with 4 prerequisites met as available"
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
(preclusion "CS3219" "CS3213")
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

; ; Math background, with first element as the default choice for module recommendation
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

; ; Communication module exemption, with first element as the default choice for module recommendation
(defrule RANK::communication-exemption-preclusion "preclusions for students with communication module exempted"
(commexempted)
=>
(assert (preclusion "CS2103" "CS2101" "CS2103T")))

(defrule RANK::communication-normal-preclusion "preclusions for students without communication module exempted"
(commnotexempted)
=>
(assert (preclusion "CS2103T" "CS2103")))

; ; Software project preference, with first element as the default choice for module recommendation

(defrule RANK::software-normal-preclusion "preclusions for students with normal software project"
(softwareprojectnormal)
=>
(assert (preclusion "CS3201" "CS3281" "CS3282" "CS3281R" "CS3282R" "CS3283" "CS3284")))

(defrule RANK::software-thematic-preclusion "preclusions for students with thematic software project"
(softwareprojectthematic)
=>
(assert (preclusion "CS3281" "CS3201" "CS3202" "CS3283" "CS3284")))

(defrule RANK::software-media-preclusion "preclusions for students with media software project"
(softwareprojectmedia)
=>
(assert (preclusion "CS3283" "CS3281" "CS3282" "CS3281R" "CS3282R" "CS3201" "CS3202")))

(defrule RANK::software-modern-preclusion "preclusions for students with modern software project"
(softwareprojectmodern)
=>
(assert (preclusion "CS3216" "CS3283" "CS3281" "CS3282" "CS3281R" "CS3282R" "CS3201" "CS3202")))

; ; Preferences for industrial experience
(defrule RANK::sip-planned
(SIP)
=>
(assert (planned "CP3200"))
(assert (planned "CP3202")))

(defrule RANK::atap-planned
(ATAP ?semester)
=>
(assert (planned "CP3880"))
(assert (skip-semester (number ?semester) (module "CP3880"))))

(defrule RANK::noc-6-month-planned
(NOCSem ?semester)
=>
(assert (planned "TR3202"))
(assert (skip-semester (number ?semester) (module "TR3202"))))

(defrule RANK::noc-1-year-planned
(NOCYear ?semester1 ?semester2)
=>
(assert (planned "TR3202"))
(assert (planned "XX3000"))
(assert (skip-semester (number ?semester1) (module "TR3202")))
(assert (skip-semester (number ?semester2) (module "XX3000"))))

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
    (printout t "Module " ?code " recommended based on preclusion list " ?preclusionlist crlf))

; ; Set module recommend field to no if it is not the default choice for a preclusion list
; ; Also reduce score to negative to prevent user from seeing them on top
(defrule RANK::set-recommendation-no
    ?module <- (module (code ?code) (recommend NONE) (status available))
    (preclusion $?preclusionlist)
    (test (member$ ?code ?preclusionlist))
    (test (neq ?code (nth$ 1 ?preclusionlist)))
    =>
    (modify ?module (recommend no) (score -90))
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

(deffunction RANK::calscore (?level ?want ?primaries ?electives ?code ?prefix ?MC ?classification ?allprojects)
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
        (bind ?score (+ 2 ?score)))

    ; ; Add score for classification-FOUNDATION modules
    (if (member$ FOUNDATION ?classification)
    then
        (bind ?score (+ 5 ?score)))
    ; ; Add score for classification-BREADTH_AND_DEPTH modules
    (if (member$ BREADTH_AND_DEPTH ?classification)
    then
        (bind ?score (+ 2 ?score)))
    ; ; Add score for classification-OTHER_REQUIRED modules
    (if (member$ OTHER_REQUIRED ?classification)
    then
        (bind ?score (+ 5 ?score)))

    ; ; Add score for project modules, make sure they have highest priority
    (if (member$ ?code ?allprojects)
    then
        (bind ?score (+ 50 ?score)))

    ; ; Reduce score for CS research-based 1MC modules
    (if (and (eq ?prefix "CS") (eq ?MC 1))
    then
        (bind ?score (- ?score 3)))
    return ?score)

(defrule RANK::assign-score
    ?module <- (module (code ?code) (status available) (classification $?classification) (level ?level) (prefix ?prefix) (MC ?MC) (want ?want&~no) (score -99))
    (primaryfocus $?primaries)
    (electivefocus $?electives)
    (allprojects $?allprojects)
    =>
    (bind ?score (calscore ?level ?want ?primaries ?electives ?code ?prefix ?MC ?classification ?allprojects))
    (modify ?module (score ?score))
    (printout t "Module " ?code " score: " ?score crlf))

; ; SELECT
; ; Selecting modules
(defrule SELECT::mark-planned "mark modules that the user plan to take"
    ?module <- (module (status available) (code ?code1))
    (planned ?code2)
    (test(eq ?code1 ?code2))
    =>
    (modify ?module (status planned))
    (printout t "Marked module " ?code1 " as planned" " total planned and taken: " (count-planned-and-taken) crlf))

(defrule SELECT::mark-taken "mark modules that the user have already taken"
    ?module <- (module (status ~taken) (code ?code1))
    (taken ?code2)
    (test(eq ?code1 ?code2))
    =>
    (modify ?module (status taken))
    (printout t "Marked module " ?code1 " as taken" " total planned and taken: " (count-planned-and-taken) crlf))

(defrule SELECT::mark-focus "mark focus area that the user plan to take"
    ?focus <- (focus (status none) (name ?name1))
    (focus-on ?name2)
    (test(eq ?name1 ?name2))
    =>
    (printout t "Marking focus area " ?name1 " as planned" crlf)
    (modify ?focus (status planned)))
