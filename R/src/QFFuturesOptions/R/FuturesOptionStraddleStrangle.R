# TODO: Add comment
# 
# Author: dhorowitz
###############################################################################


constructor('FuturesOptionStraddleStrangle', function(underlying = NULL, type = NULL, centralStrike = NULL, width = NULL){
	this <- extend(RObject(), 'FuturesOptionStraddleStrangle', .underlying = underlying, .width = width)
	
	constructorNeeds(this, underlying = 'character', width = 'numeric')
	
	if(inStaticConstructor(this)) return(this)
	
		
})
