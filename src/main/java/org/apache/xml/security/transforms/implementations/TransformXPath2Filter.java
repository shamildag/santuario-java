/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.xml.security.transforms.implementations;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.NodeFilter;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.transforms.Transform;
import org.apache.xml.security.transforms.TransformSpi;
import org.apache.xml.security.transforms.TransformationException;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.transforms.params.XPath2FilterContainer;
import org.apache.xml.security.utils.CachedXPathAPIHolder;
import org.apache.xml.security.utils.CachedXPathFuncHereAPI;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Implements the <I>XML Signature XPath Filter v2.0</I>
 *
 * @see <A HREF="http://www.w3.org/TR/xmldsig-filter2/">XPath Filter v2.0 (TR)</A>
 * @see <a HREF="http://www.w3.org/Signature/Drafts/xmldsig-xfilter2/">XPath Filter v2.0 
 * (editors copy)</a>
 */
public class TransformXPath2Filter extends TransformSpi {

    /** Field implementedTransformURI */
    public static final String implementedTransformURI =
        Transforms.TRANSFORM_XPATH2FILTER;

    /**
     * Method engineGetURI
     *
     * @inheritDoc
     */
    protected String engineGetURI() {
        return implementedTransformURI;
    }

    /**
     * Method enginePerformTransform
     * @inheritDoc
     * @param input
     *
     * @throws TransformationException
     */
    protected XMLSignatureInput enginePerformTransform(
        XMLSignatureInput input, OutputStream os, Transform transformObject
    ) throws TransformationException {
        CachedXPathAPIHolder.setDoc(transformObject.getElement().getOwnerDocument());
        try {
            List<NodeList> unionNodes = new ArrayList<NodeList>();
            List<NodeList> substractNodes = new ArrayList<NodeList>();
            List<NodeList> intersectNodes = new ArrayList<NodeList>();

            CachedXPathFuncHereAPI xPathFuncHereAPI =
                new CachedXPathFuncHereAPI(CachedXPathAPIHolder.getCachedXPathAPI());

            Element[] xpathElements =
                XMLUtils.selectNodes(
                    transformObject.getElement().getFirstChild(),
                    XPath2FilterContainer.XPathFilter2NS,
                    XPath2FilterContainer._TAG_XPATH2
                );
            if (xpathElements.length == 0) {
                Object exArgs[] = { Transforms.TRANSFORM_XPATH2FILTER, "XPath" };

                throw new TransformationException("xml.WrongContent", exArgs);
            }

            Document inputDoc = null;
            if (input.getSubNode() != null) {   
                inputDoc = XMLUtils.getOwnerDocument(input.getSubNode());
            } else {
                inputDoc = XMLUtils.getOwnerDocument(input.getNodeSet());
            }

            for (int i = 0; i < xpathElements.length; i++) {
                Element xpathElement = xpathElements[i];
                
                XPath2FilterContainer xpathContainer =
                    XPath2FilterContainer.newInstance(xpathElement, input.getSourceURI());

                NodeList subtreeRoots = 
                    xPathFuncHereAPI.selectNodeList(
                        inputDoc,
                        xpathContainer.getXPathFilterTextNode(),
                        CachedXPathFuncHereAPI.getStrFromNode(xpathContainer.getXPathFilterTextNode()),
                        xpathContainer.getElement());
                if (xpathContainer.isIntersect()) {
                    intersectNodes.add(subtreeRoots);
                } else if (xpathContainer.isSubtract()) {
                    substractNodes.add(subtreeRoots);
                } else if (xpathContainer.isUnion()) {
                    unionNodes.add(subtreeRoots);
                } 
            }

            input.addNodeFilter(
                new XPath2NodeFilter(
                    convertNodeListToSet(unionNodes),
                    convertNodeListToSet(substractNodes),
                    convertNodeListToSet(intersectNodes)
                )
            );
            input.setNodeSet(true);
            return input;
        } catch (TransformerException ex) {
            throw new TransformationException("empty", ex);
        } catch (DOMException ex) {
            throw new TransformationException("empty", ex);
        } catch (CanonicalizationException ex) {
            throw new TransformationException("empty", ex);
        } catch (InvalidCanonicalizerException ex) {
            throw new TransformationException("empty", ex);
        } catch (XMLSecurityException ex) {
            throw new TransformationException("empty", ex);
        } catch (SAXException ex) {
            throw new TransformationException("empty", ex);
        } catch (IOException ex) {
            throw new TransformationException("empty", ex);
        } catch (ParserConfigurationException ex) {
            throw new TransformationException("empty", ex);
        } 
    }
    
    static Set<Node> convertNodeListToSet(List<NodeList> l) {
        Set<Node> result = new HashSet<Node>();
        for (NodeList rootNodes : l) {
            int length = rootNodes.getLength();

            for (int i = 0; i < length; i++) {
                Node rootNode = rootNodes.item(i);
                result.add(rootNode);
            }
        }
        return result;
    }
}

class XPath2NodeFilter implements NodeFilter {
    
    boolean hasUnionNodes;
    boolean hasSubstractNodes;
    boolean hasIntersectNodes;
    Set<Node> unionNodes;
    Set<Node> substractNodes;
    Set<Node> intersectNodes;
    int inSubstract = -1;
    int inIntersect = -1;
    int inUnion = -1;
    
    XPath2NodeFilter(Set<Node> unionNodes, Set<Node> substractNodes, Set<Node> intersectNodes) {
        this.unionNodes = unionNodes;
        hasUnionNodes = !unionNodes.isEmpty();
        this.substractNodes = substractNodes;
        hasSubstractNodes = !substractNodes.isEmpty();
        this.intersectNodes = intersectNodes;
        hasIntersectNodes = !intersectNodes.isEmpty();
    }

    /**
     * @see org.apache.xml.security.signature.NodeFilter#isNodeInclude(org.w3c.dom.Node)
     */
    public int isNodeInclude(Node currentNode) {	 
        int result = 1;

        if (hasSubstractNodes && rooted(currentNode, substractNodes)) {
            result = -1;
        } else if (hasIntersectNodes && !rooted(currentNode, intersectNodes)) {
            result = 0;
        }

        //TODO OPTIMIZE
        if (result == 1) {     	        
            return 1;
        }
        if (hasUnionNodes) { 
            if (rooted(currentNode, unionNodes)) {
                return 1;
            }
            result = 0;
        }    	
        return result;
    }
    
    public int isNodeIncludeDO(Node n, int level) {
        int result = 1;
        if (hasSubstractNodes) {
            if ((inSubstract == -1) || (level <= inSubstract)) {
                if (inList(n, substractNodes)) {
                    inSubstract = level;
                } else {
                    inSubstract = -1;   			   
                }		   
            } 
            if (inSubstract != -1){
                result = -1;
            }
        } 
        if (result != -1 && hasIntersectNodes 
            && ((inIntersect == -1) || (level <= inIntersect))) { 
            if (!inList(n, intersectNodes)) {
                inIntersect = -1;
                result = 0;
            } else {
                inIntersect = level;   			   
            }		   
        }

        if (level <= inUnion) {
            inUnion = -1;
        }
        if (result == 1) {     	        
            return 1;
        }
        if (hasUnionNodes) {
            if ((inUnion == -1) && inList(n, unionNodes)) {
                inUnion = level;
            }
            if (inUnion != -1) {
                return 1;
            }
            result=0;
        }

        return result;
    }

    /**
     * Method rooted
     * @param currentNode 
     * @param nodeList 
     *
     * @return if rooted bye the rootnodes
     */
     static boolean rooted(Node currentNode, Set<Node> nodeList ) {
         if (nodeList.contains(currentNode)) {
             return true;
         }
         for (Node rootNode : nodeList) {
             if (XMLUtils.isDescendantOrSelf(rootNode, currentNode)) {
                 return true;
             }
         }
         return false;
     }

     /**
      * Method rooted
      * @param currentNode 
      * @param nodeList 
      *
      * @return if rooted bye the rootnodes
      */
     static boolean inList(Node currentNode, Set<Node> nodeList) {
         return nodeList.contains(currentNode);
     }
}
