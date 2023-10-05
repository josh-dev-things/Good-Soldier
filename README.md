Good Soldier : An Imperative, Error-Tolerant, Interpreted Programming Language.
=================================================================================
About
-----
Good Soldier Script (.goss) is a humble programming language with the following features:
- Weak Typing (Barely any typing at all)
- Low-level programming workflow (Despite being interpreted in Java)
- Slow execution (Probably, I haven't checked yet)
- **Error-Tolerance** (Will always fully interpret. Regardless of any runtime errors, the script will all be executed and interpreting won't halt)
- Surprises (Oodles of undefined behaviour)

The language is still under development and should, under no circumstances, be used for anything more than a bit of fun. The benchmark for the language was initially to solve a few [Project Euler](https://projecteuler.net/) problems, but as it is developed further and more data-types and different functionalities are added, perhaps its usage scope will grow.

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

procedure -> open expression close
open -> **"START"** (**"<-"** args)?

---
Contribution
------------
While the language is evidently in its infancy, I've made it public so that other 1st and 2nd year students at my university can contribute to it and investigate writing a parser themselves. If you'd like to contribute, please fork the repository, make your additions and then create a pull request.

**Atomic, specific and well-integrated commits are likely to get merged.**

We're looking for people to help with:
- Documentation
- Formally, well-defined grammar for the language
- Programming the parser
- Writing examples
- Testing

If you are a Java verteran or a senior honours student at the university, this project is likely not going to be a valuable use of your time. 1st and 2nd years should consider contributing!

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
