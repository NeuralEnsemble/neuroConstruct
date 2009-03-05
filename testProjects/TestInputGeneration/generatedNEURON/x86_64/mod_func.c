#include <stdio.h>
#include "hocdec.h"
extern int nrnmpi_myid;
extern int nrn_nobanner_;
modl_reg(){
  if (!nrn_nobanner_) if (nrnmpi_myid < 1) {
    fprintf(stderr, "Additional mechanisms from files\n");

    fprintf(stderr," CurrentClampVariable.mod");
    fprintf(stderr," DoubExpSyn.mod");
    fprintf(stderr," KConductance.mod");
    fprintf(stderr," LeakConductance.mod");
    fprintf(stderr," NaConductance.mod");
    fprintf(stderr," NetStimExt.mod");
    fprintf(stderr," NetStimVariable.mod");
    fprintf(stderr, "\n");
  }
  _CurrentClampVariable_reg();
  _DoubExpSyn_reg();
  _KConductance_reg();
  _LeakConductance_reg();
  _NaConductance_reg();
  _NetStimExt_reg();
  _NetStimVariable_reg();
}
