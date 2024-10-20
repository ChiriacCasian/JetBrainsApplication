Last common commits == all merge bases != all common commits

we have to find all common commits and then check for each if it is a merge base only then we can say it is a latest common commit
we check that a common commit is a merge base if it has two children that are not common commits

c15 -\
      c17 - c18 scenario 1 where both branches merge into one in the c17 commit and the children of c17 are NON-common commits so c17 is identified correctly as a merge base   
c16 -/


       /- c5 - c6
c3 - c4                  scenario2 where c3 is correctly identified as a NON-merge base and c4 is corectly identified as a merge base because it is a common commit with all children being NON-common commits
       \- c7 - c8