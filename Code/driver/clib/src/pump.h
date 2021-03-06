/*****************************************************************************
*
*	Copyright(c) 2017-2020 Author(Zhihao). As an unpublished work
*	All rights reserved.
*
*	Description:
*		This is a template file for header file
*		(Fill in a detailed description of the module's function here)
*
*	$URL: 		$
*	$Author: 	$
*	$Revision:  $
*	$Date:  	$
*
*-----------------------------------------------------------------------------
*   The information contained herein is confidential property of Zhihao
*   The use, copying, transfer or disclosure of such information is prohibited 
*   except by express written agreement with Zhihao.
*****************************************************************************/

#ifndef _PUMP_H_
#define _PUMP_H_

#ifdef __cplusplus
extern "C" {
#endif

/****************************************************************************
* #include section
*	add #include here if any
***************************************************************************/
#include "types.h"

/****************************************************************************
* #define section
*	add constant #define here if any
***************************************************************************/
#define INJECT_RESET 			 "Z0"
#define VALVE_RESET 			 "Y0"

/****************************************************************************
* ADT section
*	add Abstract Data Type definition here
***************************************************************************/


/****************************************************************************
*  extern variable declaration section
***************************************************************************/


/****************************************************************************
*  section
*	add function prototype here if any
***************************************************************************/
void pumpInit(void);

void OEMWrite(uint8_t address, uint8_t * data, uint8_t len);



#ifdef __cplusplus
}
#endif

#endif


/********************************End Of File********************************/




