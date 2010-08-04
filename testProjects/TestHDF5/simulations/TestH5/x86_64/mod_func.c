#include <stdio.h>
#include "hocdec.h"
extern int nrnmpi_myid;
extern int nrn_nobanner_;
modl_reg(){
  if (!nrn_nobanner_) if (nrnmpi_myid < 1) {
    fprintf(stderr, "Additional mechanisms from files\n");

    fprintf(stderr," DoubExpSyn.mod");
    fprintf(stderr," Gran_CaHVA_98.mod");
    fprintf(stderr," Gran_CaPool_98.mod");
    fprintf(stderr," Gran_H_98.mod");
    fprintf(stderr," Gran_KA_98.mod");
    fprintf(stderr," Gran_KCa_98.mod");
    fprintf(stderr," Gran_KDr_98.mod");
    fprintf(stderr," Gran_NaF_98.mod");
    fprintf(stderr," GranPassiveCond.mod");
    fprintf(stderr," KConductance.mod");
    fprintf(stderr," LeakConductance.mod");
    fprintf(stderr," MF_AMPA.mod");
    fprintf(stderr," NaConductance.mod");
    fprintf(stderr," NMDA.mod");
    fprintf(stderr, "\n");
  }
  _DoubExpSyn_reg();
  _Gran_CaHVA_98_reg();
  _Gran_CaPool_98_reg();
  _Gran_H_98_reg();
  _Gran_KA_98_reg();
  _Gran_KCa_98_reg();
  _Gran_KDr_98_reg();
  _Gran_NaF_98_reg();
  _GranPassiveCond_reg();
  _KConductance_reg();
  _LeakConductance_reg();
  _MF_AMPA_reg();
  _NaConductance_reg();
  _NMDA_reg();
}
