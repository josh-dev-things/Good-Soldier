Good Soldier : An Imperative, Error-Tolerant, Interpreted Programming Language.
=================================================================================
About
-----
Good Soldier Script (.goss) is a humble programming language with the following features:
- Weak Typing (Barely any typing at all)
- Low-level programming workflow (Despite being interpreted in Java)
- Slow execution (Probably, I haven't checked yet)
- Error-Tolerance (Will always fully interpret. Regardless of any runtime errors, the script will all be executed and interpreting won't halt)
- Surprises (Oodles of undefined behaviour)

The language is no longer under development and should, under no circumstances, be used for anything more than a bit of fun. The benchmark for the language was initially to solve a few [Project Euler](https://projecteuler.net/) problems, but as it is developed further and more data-types and different functionalities are added, perhaps its usage scope will grow.

_No longer being developed. I will likely re-write GOSS from the ground up another time at a much lower level once I have learnt more._

---
Installation
------------
1. Download the repository as a .zip file or fork the repository and clone it locally.
2. Go to the root of the directory and then compile the Interpreter.java file.
3. Example .goss files are in the Examples/ directory.
4. Write your own .goss, then interpret it by passing its path as a command-line argument to the interpreter.

---
Documentation
-------------
**Does not exist yet.**

_See Parser.tokenMapping for token regex definitions. See Examples/ for some usage. If unsure about particular functionality, it probably doesn't exist. In which case please implement it and contribute!_

---
Grammar
------------
Rudimentary Context-Free Grammar for Good Soldier Script:
```
procedure -> open expression* close

open -> "START" ("<-" args)?
close -> "END" ("->" args)?
args -> "["value ("," value)*"]"

expression -> tokenC | ε
tokenC -> tag | io | jump | assignment
assignment -> var "=" value
value -> var | computation | numeric | string | boolean
var -> "_*[a-zA-Z]([a-zA-Z]*[0-9]*)*"

computation -> value operator value
operator -> nOperator | sOperator | bOperator
nOperator -> "+|-|/|*|%|==|>=|<=|!="
sOperator -> "s+"|"s="|"s!="
bOperator -> "&" | "|" | "^"
numeric -> "[0-9]+(\.[0-9]+)?"
string -> "\".\""
boolean -> "TRUE" | "FALSE"

tag -> var ":"
jump -> "jump" var | "jump?" (boolean | var) var

io -> in | out
in -> "in" var
out -> "out" var
```

---
Contribution
------------
While the language is evidently in its infancy, I've made it public so that other 1st and 2nd year students at my university can contribute to it and investigate writing a parser themselves. If you'd like to contribute, please fork the repository, make your additions and then create a pull request.

**No longer in development.**
---
License
-------
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
Published under the MIT license.

---
Attributions
------------
A language by Joshua James-Lee. 

Language name inspired by The Good Soldier Svejk by Jaroslav Hašek.
