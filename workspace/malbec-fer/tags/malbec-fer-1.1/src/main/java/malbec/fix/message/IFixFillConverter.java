/**
 * 
 */
package malbec.fix.message;

import quickfix.FieldNotFound;
import quickfix.Message;

interface IFixFillConverter {
    //FixFill valueOf(ExecutionReport er) throws FieldNotFound;
    
    FixFill valueOf(Message er) throws FieldNotFound;
}