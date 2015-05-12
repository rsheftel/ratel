constructor("IMMDates", function(...)
{
    extend(RObject(), "IMMDates")
})

method("maturityFromEffective","IMMDates",function(this,effDate,tenor,isSNAC = FALSE,...)
{
    effDate <- as.POSIXct(effDate)
	if(isSNAC) effDate <- Period('days',60)$advance(effDate)
    effDate <- as.Date(effDate)
    assert(class(tenor)=="numeric" && tenor>0,paste(tenor,"is not a valid tenor"))
  
    matDate <- seq(effDate, by = paste(tenor * 12,"month",sep = " "), length = 2)[2]
    
	this$nextOne(matDate,isSNAC)
})

method("nextOne","IMMDates",function(this,myDate,isSNAC = FALSE,...)
{
    myDate <- as.POSIXct(myDate)
    myDate <- as.Date(myDate)
    
    year <- substr(myDate,1,4)
    
    m1 <- as.Date(paste(year,"-03-20",sep = ""))
    m2 <- as.Date(paste(year,"-06-20",sep = ""))
    m3 <- as.Date(paste(year,"-09-20",sep = ""))
    m4 <- as.Date(paste(year,"-12-20",sep = ""))                
    m5 <- seq(m4, by = "3 month", length = 2)[2]               
    
	if(!isSNAC){
	    if(myDate <= m1){
	    	result <- m1
	    }else if(myDate <= m2){
	        result <- m2
	    }else if(myDate <= m3){
	        result <- m3
	    }else if(myDate <= m4){
	        result <- m4
	    }else{       
	        result <- m5
	    }
	}else{
		if(myDate < m1){
			result <- m1
		}else if(myDate < m2){
			result <- m2
		}else if(myDate < m3){
			result <- m3
		}else if(myDate < m4){
			result <- m4
		}else{       
			result <- m5
		}
	}
    return(as.POSIXlt(as.character(result)))
})