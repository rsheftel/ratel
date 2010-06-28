######################################################################
##
## library: RUnit
## =====================================
##
## correctTestCase.r
## =====================================
##  test function definition which by construction executes wo error or failure
##
##
##
## Version
## =====================================
##  $Id: correctTestCase.r,v 1.2 2007/03/18 23:49:51 burgerm Exp $
##
##
######################################################################

test.correctTestCase <- function() {

  checkTrue( TRUE)
  checkTrue( !identical(TRUE, FALSE))
}
