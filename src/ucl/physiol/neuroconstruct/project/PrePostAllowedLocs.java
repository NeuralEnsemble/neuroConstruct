/**
 *  neuroConstruct
 *  Software for developing large scale 3D networks of biologically realistic neurons
 * 
 *  Copyright (c) 2009 Padraig Gleeson
 *  UCL Department of Neuroscience, Physiology and Pharmacology
 *
 *  Development of this software was made possible with funding from the
 *  Medical Research Council and the Wellcome Trust
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package ucl.physiol.neuroconstruct.project;


/**
 * Helper class for storing which of axon, dendrite, soma are allowed for pre or post connections
 *
 * @author Padraig Gleeson
 *  
 */
public class PrePostAllowedLocs 
{

    boolean somaAllowedPre = true;
    boolean axonsAllowedPre = true;
    boolean dendritesAllowedPre = false;

    boolean somaAllowedPost = true;
    boolean axonsAllowedPost = false;
    boolean dendritesAllowedPost = true;

    public boolean isAxonsAllowedPost()
    {
        return axonsAllowedPost;
    }

    public void setAxonsAllowedPost(boolean axonsAllowedPost)
    {
        this.axonsAllowedPost = axonsAllowedPost;
    }

    public boolean isAxonsAllowedPre()
    {
        return axonsAllowedPre;
    }

    public void setAxonsAllowedPre(boolean axonsAllowedPre)
    {
        this.axonsAllowedPre = axonsAllowedPre;
    }

    public boolean isDendritesAllowedPost()
    {
        return dendritesAllowedPost;
    }

    public void setDendritesAllowedPost(boolean dendritesAllowedPost)
    {
        this.dendritesAllowedPost = dendritesAllowedPost;
    }

    public boolean isDendritesAllowedPre()
    {
        return dendritesAllowedPre;
    }

    public void setDendritesAllowedPre(boolean dendritesAllowedPre)
    {
        this.dendritesAllowedPre = dendritesAllowedPre;
    }

    public boolean isSomaAllowedPost()
    {
        return somaAllowedPost;
    }

    public void setSomaAllowedPost(boolean somaAllowedPost)
    {
        this.somaAllowedPost = somaAllowedPost;
    }

    public boolean isSomaAllowedPre()
    {
        return somaAllowedPre;
    }

    public void setSomaAllowedPre(boolean somaAllowedPre)
    {
        this.somaAllowedPre = somaAllowedPre;
    }
    
    @Override
    public String toString()
    {
        StringBuffer info = new StringBuffer("Pre: ");
        if (axonsAllowedPre)
            info.append("axons ");
        if (dendritesAllowedPre)
            info.append("dends ");
        if (somaAllowedPre)
            info.append("soma ");
        
        info.append("Post: ");
        if (axonsAllowedPost)
            info.append("axons ");
        if (dendritesAllowedPost)
            info.append("dends ");
        if (somaAllowedPost)
            info.append("soma");
        
        return info.toString();
            
        
    }
    
    
}
