We have to find all common commits and then check for each if it is a merge base only then we can say it is a latest common commit
We check that a common commit is a merge base if it has two children that are not common commits

c15 -\
      c17 - c18         scenario 1 : where both branches merge into one in the c17 commit and the children of c17 are NON-common commits so c17 is identified correctly as a merge base   
c16 -/


       /- c5 - c6
c3 - c4                  scenario 2 : where c3 is correctly identified as a NON-merge base and c4 is correctly identified as a merge base because it is a common commit with all children being NON-common commits
       \- c7 - c8

Rule for programatically checking if a commit is a merge base : 
    - we cosider all sha1s from one branch in reverse-chronological order,
    - if the current sha is common and the next one is not then this is a merge base
    - if the current sha is NON-common and the next one is common then THE NEXT ONE is a merge base

* if the last commit (the first one chronologically) is common and a merge base it will be picked up by the second rule

To-Do:
add pagination support for very large projects (currently supports only 100 commits)