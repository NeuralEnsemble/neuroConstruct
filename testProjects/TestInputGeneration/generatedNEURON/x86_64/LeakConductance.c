/* Created by Language version: 6.0.2 */
/* VECTORIZED */
#include <stdio.h>
#include <math.h>
#include "scoplib.h"
#undef PI
 
#include "md1redef.h"
#include "section.h"
#include "nrnoc_ml.h"
#include "md2redef.h"

#if METHOD3
extern int _method3;
#endif

#undef exp
#define exp hoc_Exp
extern double hoc_Exp();
 	/*SUPPRESS 761*/
	/*SUPPRESS 762*/
	/*SUPPRESS 763*/
	/*SUPPRESS 765*/
	 extern double *getarg();
 static double **_p; static Datum **_ppvar;
 
#define delta_t dt
#define gmax _p[_ix][0]
#define e _p[_ix][1]
#define i _p[_ix][2]
#define v _p[_ix][3]
#define _g _p[_ix][4]
 
#if MAC
#if !defined(v)
#define v _mlhv
#endif
#if !defined(h)
#define h _mlhh
#endif
#endif
 static int hoc_nrnpointerindex =  -1;
 /* external NEURON variables */
 extern double dt;
 extern double t;
 /* declaration of user functions */
 static int _mechtype;
extern int nrn_get_mechtype();
 static _hoc_setdata() {
 Prop *_prop, *hoc_getdata_range();
 _prop = hoc_getdata_range("LeakConductance");
 _p = &_prop->param; _ppvar = &_prop->dparam;
 ret(1.);
}
 /* connect user functions to hoc names */
 static IntFunc hoc_intfunc[] = {
 "setdata_LeakConductance", _hoc_setdata,
 0, 0
};
 /* declare global and static user variables */
 /* some parameters have upper and lower limits */
 static HocParmLimits _hoc_parm_limits[] = {
 0,0,0
};
 static HocParmUnits _hoc_parm_units[] = {
 "gmax_LeakConductance", "S/cm2",
 "e_LeakConductance", "mV",
 "i_LeakConductance", "mA/cm2",
 0,0
};
 /* connect global user variables to hoc */
 static DoubScal hoc_scdoub[] = {
 0,0
};
 static DoubVec hoc_vdoub[] = {
 0,0,0
};
 static double _sav_indep;
 static nrn_alloc(), nrn_init(), nrn_state();
 static nrn_cur(), nrn_jacob();
 /* connect range variables in _p that hoc is supposed to know about */
 static char *_mechanism[] = {
 "6.0.2",
"LeakConductance",
 "gmax_LeakConductance",
 "e_LeakConductance",
 0,
 "i_LeakConductance",
 0,
 0,
 0};
 
static nrn_alloc(_prop)
	Prop *_prop;
{
	Prop *prop_ion, *need_memb();
	double *_p[1]; Datum *_ppvar[1]; int _ix = 0;
 	_p[0] = nrn_prop_data_alloc(_mechtype, 5);
 	/*initialize range parameters*/
 	gmax = 0.0003;
 	e = -54.3;
 	_prop->param = _p[0];
 	_prop->param_size = 5;
 
}
 static _initlists();
 _LeakConductance_reg() {
	int _vectorized = 1;
 	double* _x = &t;
	_p = &_x;
 _initlists();
 	register_mech(_mechanism, nrn_alloc,nrn_cur, nrn_jacob, nrn_state, nrn_init, hoc_nrnpointerindex, _vectorized);
 _mechtype = nrn_get_mechtype(_mechanism[1]);
  hoc_register_dparam_size(_mechtype, 0);
 	hoc_register_var(hoc_scdoub, hoc_vdoub, hoc_intfunc);
 	ivoc_help("help ?1 LeakConductance /home/matteo/neuroConstruct/testProjects/TestInputGeneration/generatedNEURON/x86_64/LeakConductance.mod\n");
 hoc_register_limits(_mechtype, _hoc_parm_limits);
 hoc_register_units(_mechtype, _hoc_parm_units);
 }
static int _reset;
static char *modelname = "Channel: LeakConductance";

static int error;
static int _ninits = 0;
static int _match_recurse=1;
static _modl_cleanup(){ _match_recurse=1;}

static initmodel(_ix) int _ix; {
  int _i; double _save;{

}
}

static nrn_init(_Memb_list* _ml, int _type_ignore) {
	int _count = _ml->_nodecount; Node** _nodes = _ml->_nodelist;
#if CACHEVEC
	int *_ni = _ml->_nodeindices;
#endif
  int _ix; double _v;
 _p = _ml->_data; _ppvar = _ml->_pdata;

#if _CRAY
#pragma _CRI ivdep
#endif
	for (_ix = 0; _ix < _count; ++_ix) {
#if CACHEVEC
  if (use_cachevec) {
    _v = VEC_V(_ni[_ix]);
  }else
#endif
  {
    _v = NODEV(_nodes[_ix]);
  }
		v = _v;
		initmodel(_ix);
	}
}

static double _nrn_current(_ix, _v) int _ix; double _v;{
 double _current=0.;v=_v;
{ {
   i = gmax * ( v - e ) ;
   }
 _current += i;

} return _current;
}

static nrn_cur(_Memb_list* _ml, int _type_ignore) {
	int _count = _ml->_nodecount;  Node** _nodes = _ml->_nodelist;
#if CACHEVEC
	int *_ni = _ml->_nodeindices;
#endif
int _ix;
 _p = _ml->_data; _ppvar = _ml->_pdata;

#if _CRAY
#pragma _CRI ivdep
#endif
	for (_ix = 0; _ix < _count; ++_ix) {
		double _rhs, _v;
#if CACHEVEC
  if (use_cachevec) {
    _v = VEC_V(_ni[_ix]);
  }else
#endif
  {
    _v = NODEV(_nodes[_ix]);
  }
		_g = _nrn_current(_ix, _v + .001);
 	{
		_rhs = _nrn_current(_ix, _v);
 	}
		_g = (_g - _rhs)/.001;
#if CACHEVEC
  if (use_cachevec) {
	VEC_RHS(_ni[_ix]) -= _rhs;
  }else
#endif
  {
	NODERHS(_nodes[_ix]) -= _rhs;
  }
	}

}

static nrn_jacob(_Memb_list* _ml, int _type_ignore) {
	int _count = _ml->_nodecount; Node** _nodes = _ml->_nodelist;
#if CACHEVEC
	int *_ni = _ml->_nodeindices;
#endif
int _ix;
 _p = _ml->_data;

#if _CRAY
#pragma _CRI ivdep
#endif
	for (_ix = 0; _ix < _count; ++_ix) {
#if CACHEVEC
  if (use_cachevec) {
	VEC_D(_ni[_ix]) += _g;
  }else
#endif
  {
	NODED(_nodes[_ix]) += _g;
  }
   }

}

static nrn_state(_Memb_list* _ml, int _type_ignore) {
	int _count = _ml->_nodecount; Node** _nodes = _ml->_nodelist;
#if CACHEVEC
	int *_ni = _ml->_nodeindices;
#endif
}

static terminal(){}

static _initlists(){
 int _i; int _ix=0; static int _first = 1;
  if (!_first) return;
_first = 0;
}
