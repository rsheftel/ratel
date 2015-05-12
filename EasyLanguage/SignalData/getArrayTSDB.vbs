Sub getArrayTSDB
  dim RObj
  dim ADEbarID, ADEclose
  dim index, sizeRclose

  dim temp
  
'Set up R object using R(D)COM
  set RObj = CreateObject("StatConnectorSrv.StatConnector")

'Initiate Background R session
  RObj.Init("R")

'Set up the EL variables
  set ADEbarID = Variables("ADEbarID")
  set ADEclose = Variables("ADEclose")

'Load the csv file into R
  RObj.EvaluateNoReturn("file<-'//laptop/ryan/signaldata1.csv'")
  RObj.evaluatenoreturn("signalMatrix <- read.csv(file)")

'Populate the EL variables
  sizeRclose = RObj.evaluate("nrow(signalMatrix)-1")
  Variables("maxIndex").AsDouble(0) = sizeRclose
  for index = 0 to sizeRclose
    ADEbarID.SelectedIndex(0) = index
    ADEbarID.AsDouble(0) = RObj.Evaluate("signalMatrix["&index+1&",'RbarID']")
    ADEclose.SelectedIndex(0) = index
    ADEclose.AsDouble(0) = RObj.Evaluate("signalMatrix["&index+1&",'Rclose']")
  next

'Clean up and close the R session
  RObj.Close
End Sub