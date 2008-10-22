/**
 * neuroConstruct
 *
 * Software for developing large scale 3D networks of biologically realistic neurons
 * Copyright (c) 2008 Padraig Gleeson
 * UCL Department of Physiology
 *
 * Development of this software was made possible with funding from the
 * Medical Research Council
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
