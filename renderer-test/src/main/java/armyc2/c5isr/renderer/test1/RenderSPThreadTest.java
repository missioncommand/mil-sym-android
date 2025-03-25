/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package armyc2.c5isr.renderer.test1;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseArray;
import armyc2.c5isr.renderer.MilStdIconRenderer;
import armyc2.c5isr.renderer.utilities.ImageInfo;
import armyc2.c5isr.renderer.utilities.MilStdAttributes;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 *
 */
public class RenderSPThreadTest implements Runnable
{
    boolean _result = true;
    long _failCount = 0;
    long _successCount = 0;
    ArrayList<String> _errors = new ArrayList<String>();

    String _symbolID = "";
    String _name = "";
    boolean _randomAffiliation = false;
    boolean _done = false;
    
    private boolean threadTest(String symbolID, String threadName, boolean randomAffiliation)
    {
        MilStdIconRenderer mir = MilStdIconRenderer.getInstance();
        String foo = "UFHNPASGWDLMJK";
        String foo2 = "APCDXF";
        char[] affiliations = foo.toCharArray();
        char[] stati = foo2.toCharArray();
        Map<String,String> modifiers = new HashMap<>();
    	//populateModifiersForUnits(modifiers);
        Map<String,String> attributes = new HashMap<>();
    	
        String id = new String(symbolID);
    	int count = 1000;
    	float fcount = count;
        ImageInfo ii= null;
        boolean success = true;
    	for(int i = 1; i <= count; i++)
    	{
            if(randomAffiliation)
            {
                Random r = new Random();
                char affiliation = affiliations[r.nextInt(13)];
                char status = affiliations[r.nextInt(5)];
                id = id.substring(0, 1) + affiliation + id.substring(2,3) + status + id.substring(4);
            }
            ii = mir.RenderIcon(id, modifiers, attributes);
            if(ii == null || ii.getImage() == null)
                success = false;
                
            if(i % 100 == 0)
            {
                    String message = symbolID + ": " + String.valueOf((int)(i/fcount * 100f)) + "% complete";
                    Log.i("threadTest",message);
            }
    	}
        _done = true;
        Log.i("threadTest","Thread \"" + threadName + "\" Done");
        Log.println(Log.INFO,threadName," done.");
        if(success == false)
            Log.println(Log.INFO,threadName," failed to render all symbols.");
        
        threadName = threadName + ": " + String.valueOf(success);
        return success;

    }
    
    public void run()
    {

        Random r = new Random();
        String[] symbolIDs = {"SUPP-----------","SUPPT----------","SUPPV----------","SUPPT----------","SUPPL----------"};
        _result = threadTest(symbolIDs[r.nextInt(4)], _name, _randomAffiliation);//*/
        //S*A*MFQN--*****
    }//*/
    
    public boolean getResult()
    {
        return _result;
    }

    public void testRenderer() throws InterruptedException
    {
        /*MilStdIconRenderer renderer = MilStdIconRenderer.getInstance();
        String symbolCode = "S*A*MFQN--*****";
        SparseArray<String> modifiers = new SparseArray<String>();
        SparseArray<String> attributes = new SparseArray<String>();
        attributes.put(MilStdAttributes.SymbologyStandard, "0");

        ImageInfo imageInfo = renderer.RenderIcon(symbolCode, modifiers, attributes);
        Bitmap bitmap = imageInfo.getImage();
        ByteBuffer buffer = ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(buffer);
        byte[] imageBytes = buffer.array();//*/
        int len = 3136;//imageBytes.length;//3136
        byte check = -34;//imageBytes[847];//-34

        //RendererSettings.getInstance().setCacheSize(20);


        int numberOfThreads = 20;
        CountDownLatch doneSignal = new CountDownLatch(numberOfThreads);
        CountDownLatch startSignal = new CountDownLatch(1);
        ArrayList<SecImageClass> secImageClasses = new ArrayList<SecImageClass>();
        ArrayList<agnosticThread> threads = new ArrayList<agnosticThread>();

        for(int i = 0; i < numberOfThreads; i++)
        {
            SecImageClass secImageClass = new SecImageClass();
            secImageClasses.add(secImageClass);
            agnosticThread r = new agnosticThread(secImageClass, doneSignal, startSignal);
            Thread thread = new Thread(r);
            threads.add(r);
            thread.start();
        }

        startSignal.countDown();

        doneSignal.await();
        boolean error = false;
        agnosticThread ttemp = null;
        for (int i = 0; i < threads.size(); i++)
        {
            ttemp = threads.get(i);
            Log.i("Thread " + String.valueOf(i),"Results:");
            for (int j = 0; j < 100; j++)
            {
                error = false;
                if(ttemp.lengths[j] != len){
                    Log.i("-","bytes not long enough");
                    _errors.add("bytes not long enough");
                    _failCount++;
                    error = true;
                }
                if(ttemp.checks[j] != check){
                    Log.i("-","zeroed out");
                    _errors.add("zeroed out");
                    _failCount++;
                    error = true;
                }
                if(error == false)
                    _successCount++;
                //assertEquals("bytes not long enough", 3136, secImageClass.imageBytes.length);
                //assertEquals("zeroed out", -22, secImageClass.imageBytes[847]);
            }
            Log.i("SUCCESS: ",String.valueOf(_successCount));
            Log.i("----------------","--------------");
            _successCount = 0;
            _failCount = 0;

        }

    }

    /**
     * class to hold data and test
     */
    class SecImageClass
    {
        public byte[] imageBytes;
        private Map<String,String> modifiers = new HashMap<>();
        private Map<String,String> attributes = new HashMap<>();
        private String symbolCode = "S*A*MFQN--*****";
        private MilStdIconRenderer renderer = MilStdIconRenderer.getInstance();

        public void setImageInfo()
        {
            ImageInfo imageInfo = renderer.RenderIcon(symbolCode, modifiers, attributes);
            Bitmap bitmap = imageInfo.getImage();
            ByteBuffer buffer = ByteBuffer.allocate(bitmap.getByteCount());
            bitmap.copyPixelsToBuffer(buffer);
            imageBytes = buffer.array();
        }

    }



    /**
     * Thread for implementation agnostic testing of Renderer,
     * Creates 100 copies
     */
    class agnosticThread implements Runnable
    {
        byte[] checks = new byte[100];
        int[] lengths = new int[100];
        SecImageClass mImage;
        CountDownLatch mDoneSignal;
        CountDownLatch mStartSignal;


        public agnosticThread(SecImageClass image, CountDownLatch doneSignal,
                              CountDownLatch startSignal)
        {
            mImage = image;
            mDoneSignal = doneSignal;
            mStartSignal = startSignal;
        }


        /**
         * Starts executing the active part of the class' code. This method is
         * called when a thread is started that has been created with a class which
         * implements {@code Runnable}.
         */
        @Override
        public void run()
        {
            try
            {
                mStartSignal.await();
                for(int i = 0; i < 100; i++)
                {
                    mImage.setImageInfo();
                    lengths[i] = mImage.imageBytes.length;
                    checks[i] = (mImage.imageBytes[847]);
                }
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            mDoneSignal.countDown();
        }
    }
    

}
