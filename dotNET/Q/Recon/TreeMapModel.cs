using System.Collections.Generic;

namespace Q.Recon {
    public interface TreeMapModel {
        List<QNode> nodes();
        void setIsEqualSizes(bool newSetting);
    }
}