package com.example.twist_talk;

public class Posts
{
    public String uid, time, date, postimage, description, profileimage, fullname, username;

    public Posts()
    {

    }

    public Posts(String uid, String time, String date, String postimage, String description, String profileimage, String fullname, String username)
    {
        this.uid = uid;
        this.time = time;
        this.date = date;
        this.postimage = postimage;
        this.description = description;
        this.profileimage = profileimage;
        this.fullname = fullname;
        this.username = username;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }


    public String getUid()
    {
        return uid;
    }

    public void setUid(String uid)
    {
        this.uid = uid;
    }

    public String getTime()
    {
        return time;
    }

    public void setTime(String time)
    {
        this.time = time;
    }

    public String getDate()
    {
        return date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public String getPostimage()
    {
        return postimage;
    }

    public void setPostimage(String postimage)
    {
        this.postimage = postimage;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getProfileimage()
    {
        return profileimage;
    }

    public void setProfileimage(String profileimage)
    {
        this.profileimage = profileimage;
    }

    public String getFullname()
    {
        return fullname;
    }

    public void setFullname(String fullname)
    {
        this.fullname = fullname;
    }
}
