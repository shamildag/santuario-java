/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * imitations under the License.
 */

/*
 * XSEC
 *
 * XKMSKeyBinding := Interface for KeyBinding elements
 *
 * $Id$
 *
 */

#ifndef XKMSKEYBINDING_INCLUDE
#define XKMSKEYBINDING_INCLUDE

// XSEC Includes

#include <xsec/framework/XSECDefs.hpp>
#include <xsec/xkms/XKMSKeyBindingAbstractType.hpp>

/**
 * @ingroup xkms
 * @{
 */

/**
 * @brief Interface definition for the KeyBinding elements
 *
 * The \<KeyBinding\> Element is used in a result message to a client
 * to provide information on a particular key.
 *
 * The schema definition for KeyBinding is as follows :
 *
 * \verbatim
   <!-- KeyBinding -->
   <element name="KeyBinding" type="xkms:KeyBindingType"/>
   <complexType name="KeyBindingType">
      <complexContent>
         <extension base="xkms:UnverifiedKeyBindingType">
            <sequence>
               <element ref="xkms:Status"/>
            </sequence>
         </extension>
      </complexContent>
   </complexType>
   <!-- /KeyBinding -->\endverbatim
 */


class XKMSKeyBinding : public XKMSKeyBindingAbstractType {

	/** @name Constructors and Destructors */
	//@{

protected:

	XKMSKeyBinding() {};

public:

	virtual ~XKMSKeyBinding() {};


private:

	// Unimplemented
	XKMSKeyBinding(const XKMSKeyBinding &);
	XKMSKeyBinding & operator = (const XKMSKeyBinding &);

};

#endif /* XKMSKEYBINDING_INCLUDE */
