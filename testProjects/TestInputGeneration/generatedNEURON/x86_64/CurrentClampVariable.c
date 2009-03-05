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
#define del _p[_ix][0]
#define dur _p[_ix][1]
#define amp _p[_ix][2]
#define i _p[_ix][3]
#define v _p[_ix][4]
#define _g _p[_ix][5]
#define _nd_area  *_ppvar[_ix][0].pval
 
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
 extern Prop* nrn_point_prop_;
 static int _pointtype;
 static void* _hoc_create_pnt(_ho) Object* _ho; { void* create_point_process();
 return create_point_process(_pointtype, _ho);
}
 static void _hoc_destroy_pnt();
 static double _hoc_loc_pnt(_vptr) void* _vptr; {double loc_point_process();
 return loc_point_process(_pointtype, _vptr);
}
 static double _hoc_has_loc(_vptr) void* _vptr; {double has_loc_point();
 return has_loc_point(_vptr);
}
 static double _hoc_get_loc_pnt(_vptr)void* _vptr; {
 double get_loc_point_process(); return (get_loc_point_process(_vptr));
}
 static _hoc_setdata(_vptr) void* _vptr; { Prop* _prop;
 _prop = ((Point_process*)_vptr)->_prop;
 _p = &_prop->param; _ppvar = &_prop->dparam;
 }
 /* connect user functions to hoc names */
 static IntFunc hoc_intfunc[] = {
 0,0
};
 static struct Member_func {
	char* _name; double (*_member)();} _member_func[] = {
 "loc", _hoc_loc_pnt,
 "has_loc", _hoc_has_loc,
 "get_loc", _hoc_get_loc_pnt,
 0, 0
};
 /* declare global and static user variables */
 /* some parameters have upper and lower limits */
 static HocParmLimits _hoc_parm_limits[] = {
 "dur", 0, 1e+09,
 0,0,0
};
 static HocParmUnits _hoc_parm_units[] = {
 "del", "ms",
 "dur", "ms",
 "amp", "nA",
 "i", "nA",
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
 static void _hoc_destroy_pnt(_vptr) void* _vptr; {
   destroy_point_process(_vptr);
}
 /* connect range variables in _p that hoc is supposed to know about */
 static char *_mechanism[] = {
 "6.0.2",
"CurrentClampVariable",
 "del",
 "dur",
 "amp",
 0,
 "i",
 0,
 0,
 0};
 
static nrn_alloc(_prop)
	Prop *_prop;
{
	Prop *prop_ion, *need_memb();
	double *_p[1]; Datum *_ppvar[1]; int _ix = 0;
  if (nrn_point_prop_) {
	_p[0] = nrn_point_prop_->param;
	_ppvar[0] = nrn_point_prop_->dparam;
 }else{
 	_p[0] = nrn_prop_data_alloc(_mechtype, 6);
 	/*initialize range parameters*/
 	del = 100;
 	dur = 800;
 	amp = 0.2;
  }
 	_prop->param = _p[0];
 	_prop->param_size = 6;
  if (!nrn_point_prop_) {
 	_ppvar[0] = nrn_prop_datum_alloc(_mechtype, 2);
  }
 	_prop->dparam = _ppvar[0];
 	/*connect ionic variables to this model*/
 
}
 static _initlists();
 _CurrentClampVariable_reg() {
	int _vectorized = 1;
 	double* _x = &t;
	_p = &_x;
 _initlists();
 	_pointtype = point_register_mech(_mechanism,
	 nrn_alloc,nrn_cur, nrn_jacob, nrn_state, nrn_init,
	 hoc_nrnpointerindex,
	 _hoc_create_pnt, _hoc_destroy_pnt, _member_func,
	 _vectorized);
 _mechtype = nrn_get_mechtype(_mechanism[1]);
  hoc_register_dparam_size(_mechtype, 2);
 	hoc_register_var(hoc_scdoub, hoc_vdoub, hoc_intfunc);
 	ivoc_help("help ?1 CurrentClampVariable /home/matteo/neuroConstruct/testProjects/TestInputGeneration/generatedNEURON/x86_64/CurrentClampVariable.mod\n");
 hoc_register_limits(_mechtype, _hoc_parm_limits);
 hoc_register_units(_mechtype, _hoc_parm_units);
 }
static int _reset;
static char *modelname = "";

static int error;
static int _ninits = 0;
static int _match_recurse=1;
static _modl_cleanup(){ _match_recurse=1;}

static initmodel(_ix) int _ix; {
  int _i; double _save;{
 {
   i = 0.0 ;
   }

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
#if EXTRACELLULAR
 if (_nodes[_ix]->_extnode) {
    _v = NODEV(_nodes[_ix]) +_nodes[_ix]->_extnode->_v[0];
 }else
#endif
 {
#if CACHEVEC
  if (use_cachevec) {
    _v = VEC_V(_ni[_ix]);
  }else
#endif
  {
    _v = NODEV(_nodes[_ix]);
  }
 }
		v = _v;
		initmodel(_ix);
	}
}

static double _nrn_current(_ix, _v) int _ix; double _v;{
 double _current=0.;v=_v;
{ {
   at_time ( del ) ;
   at_time ( del + dur ) ;
   if ( t < del + dur  && t >= del ) {
     i = 0.2 * sin ( 2.0 * 3.14159265 * t / 200.0 ) ;
     }
   else {
     i = 0.0 ;
     }
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
#if EXTRACELLULAR
 if (_nodes[_ix]->_extnode) {
    _v = NODEV(_nodes[_ix]) +_nodes[_ix]->_extnode->_v[0];
 }else
#endif
 {
#if CACHEVEC
  if (use_cachevec) {
    _v = VEC_V(_ni[_ix]);
  }else
#endif
  {
    _v = NODEV(_nodes[_ix]);
  }
 }
		_g = _nrn_current(_ix, _v + .001);
 	{
		_rhs = _nrn_current(_ix, _v);
 	}
		_g = (_g - _rhs)/.001;
	_g *= 1.e2/(_nd_area);
	_rhs *= 1.e2/(_nd_area);
#if CACHEVEC
  if (use_cachevec) {
	VEC_RHS(_ni[_ix]) += _rhs;
  }else
#endif
  {
	NODERHS(_nodes[_ix]) += _rhs;
  }
#if EXTRACELLULAR
 if (_nodes[_ix]->_extnode) {
   *_nodes[_ix]->_extnode->_rhs[0] += _rhs;
 }
#endif
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
	VEC_D(_ni[_ix]) -= _g;
  }else
#endif
  {
	NODED(_nodes[_ix]) -= _g;
  }
#if EXTRACELLULAR
 if (_nodes[_ix]->_extnode) {
   *_nodes[_ix]->_extnode->_d[0] += _g;
 }
#endif
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
